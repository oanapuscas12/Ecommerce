const express = require('express');
const bodyParser = require('body-parser');
const axios = require('axios');
const { Pool } = require('pg');

const app = express();
const port = process.env.PORT || 3000;

app.use(bodyParser.json());
app.use(express.static('public'));

const pool = new Pool({
    user: 'DB_USER',
    host: 'DB_HOST',
    database: 'DB_DATABASE',
    password: 'DB_PASSWORD',
    port: 'DB_PORT',
});

app.use(express.static('src'));

app.get('/', (req, res) => {
    res.sendFile(__dirname + '/public/form.html');
});

app.post('/merchant_data', async (req, res) => {
    const { cui } = req.body;
    const currentDate = getCurrentDate();

    const requestBody = [
        {
            "cui": parseInt(cui),
            "data": currentDate
        }
    ];

    try {
        const apiResponse = await axios.post('https://webservicesp.anaf.ro/PlatitorTvaRest/api/v8/ws/tva', requestBody, {
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (apiResponse.status !== 200) {
            throw new Error(`API request failed with status ${apiResponse.status}`);
        }

        const apiResult = apiResponse.data;
        res.json(apiResult);
    } catch (error) {
        console.error('Error making API request:', error.message);
        if (error.response) {
            console.error('API response error data:', error.response.data);
            res.status(error.response.status).json({ error: error.response.data });
        } else if (error.request) {
            console.error('No response received:', error.request);
            res.status(500).json({ error: 'No response received from API' });
        } else {
            console.error('Error setting up request:', error.message);
            res.status(500).json({ error: 'Error setting up request' });
        }
    }
});

app.post('/check_field', async (req, res) => {
    const { field, value } = req.body;
    try {
        const query = `SELECT 1 FROM form_users WHERE ${field} = $1`;
        const result = await pool.query(query, [value]);
        res.json({ exists: result.rowCount > 0 });
    } catch (error) {
        console.error(`Error checking ${field} in PostgreSQL:`, error);
        res.status(500).send(`Error checking ${field}`);
    }
});

app.post('/submit_data', async (req, res) => {
    const { cui, denumire, adresa, nr_reg_com, telefon, cod_postal, email, name, user_name } = req.body;
    let client;

    try {
        client = await pool.connect();
        await client.query('BEGIN');

        const query = `
            INSERT INTO form_users (cui, denumire, adresa, nr_reg_com, telefon, cod_postal, email, name, user_name)
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
            RETURNING id`;
        const values = [cui, denumire, adresa, nr_reg_com, telefon, cod_postal, email, name, user_name];
        const result = await client.query(query, values);

        await client.query('COMMIT');

        res.status(200).send('success');
    } catch (error) {
        console.error('Error inserting into PostgreSQL:', error);
        if (client) {
            await client.query('ROLLBACK');
        }
        res.status(500).send('failure');
    } finally {
        if (client) {
            client.release();
        }
    }
});

function getCurrentDate() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

app.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});

const express = require('express');
const bodyParser = require('body-parser');
const bcrypt = require('bcrypt');
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

app.post('/check_existing', async (req, res) => {
    const { field, value } = req.body;

    const merchantFields = ['cui', 'legal_business_name', 'address', 'nr_reg_com', 'phone_number', 'postal_code', 'country', 'county', 'city', 'industry'];
    const userFields = ['email', 'user_name', 'name'];

    const nonExactMatchFields = ['address', 'postal_code', 'country', 'county', 'city', 'industry'];

    let table;
    let query;
    let queryParams;

    if (merchantFields.includes(field)) {
        table = 'merchants';
    } else if (userFields.includes(field)) {
        table = 'users';
    } else {
        return res.status(400).json({ error: `Invalid field: ${field}` });
    }

    if (nonExactMatchFields.includes(field)) {
        query = `SELECT 1 FROM ${table} WHERE ${field} = $1`;
        queryParams = [value];
    } else {
        query = `SELECT 1 FROM ${table} WHERE ${field} = $1`;
        queryParams = [value];
    }

    try {
        const result = await pool.query(query, queryParams);
        const exists = nonExactMatchFields.includes(field) ? false : result.rowCount > 0;
        
        if (nonExactMatchFields.includes(field)) {
            res.json({ exists: false });
        } else {
            res.json({ exists });
        }
    } catch (error) {
        console.error(`Error checking ${field} in ${table} table:`, error);
        res.status(500).send(`Error checking ${field}`);
    }
});

app.post('/submit_data', async (req, res) => {
    const {
        cui,
        legal_business_name,
        address,
        nr_reg_com,
        phone_number,
        postal_code,
        country,
        county,
        city,
        industry,
        email,
        name,
        user_name
    } = req.body;
    let client;

    const defaultPassword = 'password';

    try {
        client = await pool.connect();
        await client.query('BEGIN');

        const hashedPassword = await bcrypt.hash(defaultPassword, 12);
        const currentTimestamp = getCurrentTimestamp();
        const userQuery = `
            INSERT INTO users (user_name, name, email, password, is_admin, is_active, version, created_by, created_date, last_update_date, updated_by)
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
            RETURNING id`;
        const userValues = [user_name, name, email, hashedPassword, false, false, 0, user_name, currentTimestamp, currentTimestamp, user_name];
        const userResult = await client.query(userQuery, userValues);
        const userId = userResult.rows[0].id;

        const merchantQuery = `
            INSERT INTO merchants (id, cui, legal_business_name, address, nr_reg_com, phone_number, postal_code, country, county, city, industry, is_store_active, is_store_launched)
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
            RETURNING id`;
        const merchantValues = [
            userId,
            cui,
            legal_business_name,
            address,
            nr_reg_com,
            phone_number,
            postal_code,
            country,
            county,
            city,
            industry,
            false,
            false
        ];
        await client.query(merchantQuery, merchantValues);

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

function getCurrentTimestamp() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    const milliseconds = String(now.getMilliseconds()).padStart(3, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}.${milliseconds}`;
}

app.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});
in the project run the command:
npm install --save express
npm install bcrypt
--------------
Connect to postgres,
Complete in app.js the DB information:
user: 'DB_USERNAME',
host: 'DB_HOST',
database: 'DB_DATABASE_NAME',
password: 'DB_PASSWORD',
port: 'DB_PORT',
---------------
create the table 'merchants' in your db with the query command:
CREATE TABLE merchants (
    id SERIAL PRIMARY KEY,
    cui VARCHAR(50) NOT NULL,
    denumire VARCHAR(255) NOT NULL,
    adresa VARCHAR(255) NOT NULL,
    nr_reg_com VARCHAR(50) NOT NULL,
    telefon VARCHAR(50) NOT NULL,
    cod_postal VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    user_name VARCHAR(100) NOT NULL
);
---------------
in the project run the command: npm start

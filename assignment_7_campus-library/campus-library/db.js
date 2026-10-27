// db.js - shared connection pool to MySQL
const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 4444,
  user: 'root',
  password: 'Iamawalker#1',
  database: 'gqldemo',
});

module.exports = pool;
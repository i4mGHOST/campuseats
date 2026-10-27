// db.js - shared connection pool to MySQL for CampusEats
const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 4444,
  user: 'root',
  password: 'Iamawalker#1',
  database: 'campuseats_db',
});

module.exports = pool;
const express = require('express');
const router = express.Router();
const db = require('../db'); // Adjust path to your database pool connection

// GET /api/pets - Retrieve all pets for the catalogue
router.get('/', async (req, res) => {
    try {
        const result = await db.query('SELECT * FROM pets ORDER BY id ASC');
        res.json(result.rows);
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ error: 'Server error while fetching pets' });
    }
});

module.exports = router;
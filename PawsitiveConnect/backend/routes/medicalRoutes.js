const express = require('express');
const router = express.Router();
const db = require('../db'); // Adjust path if your database connection file is named differently

// GET /api/medical - Retrieve all medical records
router.get('/', async (req, res) => {
    try {
        // Fetch records ordered by the most recent dates first
        const result = await db.query('SELECT * FROM medical_records ORDER BY date_administered DESC');
        res.json(result.rows);
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ error: 'Server error while fetching medical records' });
    }
});

module.exports = router;
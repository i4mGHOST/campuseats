const express = require('express');
const router = express.Router();
const db = require('../db');

// GET /api/rescue - Retrieve all rescue dispatches
router.get('/', async (req, res) => {
    try {
        // Fetch active incidents ordered by the newest reports first
        const result = await db.query('SELECT * FROM rescue_dispatches ORDER BY report_time DESC');
        res.json(result.rows);
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ error: 'Server error while fetching rescue dispatches' });
    }
});

module.exports = router;
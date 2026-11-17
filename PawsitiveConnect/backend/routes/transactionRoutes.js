const express = require('express');
const router = express.Router();
const { getTransactions, processPayment } = require('../controllers/transactionController');

// GET /api/transactions
router.get('/', getTransactions);

// POST /api/transactions/process
router.post('/process', processPayment);

module.exports = router;
const pool = require('../db');

// GET: Fetch all transactions
const getTransactions = async (req, res) => {
    try {
        const query = `SELECT * FROM transactions ORDER BY transaction_date DESC;`;
        const result = await pool.query(query);
        res.status(200).json(result.rows);
    } catch (err) {
        console.error("Error fetching transactions:", err);
        res.status(500).json({ error: "Failed to fetch transactions" });
    }
};

// POST: Process an adoption payment (Atomic Transaction)
const processPayment = async (req, res) => {
    const { application_id, payer_name, amount, payment_method } = req.body;

    // Acquire a dedicated client from the pool for a transaction block
    const client = await pool.connect();

    try {
        await client.query('BEGIN'); // Start atomic transaction

        // Alt Fragment: Check if the application is still APPROVED (pet hasn't been re-routed)
        const appCheck = await client.query(
            `SELECT status FROM adoption_applications WHERE application_id = $1`,
            [application_id]
        );

        if (appCheck.rows.length === 0 || appCheck.rows[0].status !== 'APPROVED') {
            await client.query('ROLLBACK');
            return res.status(400).json({ error: "Payment rejected: Application is not approved or pet is unavailable." });
        }

        // 1. Insert the new transaction record
        await client.query(
            `INSERT INTO transactions (application_id, payer_name, amount, payment_method, status)
             VALUES ($1, $2, $3, $4, 'COMPLETED')`,
            [application_id, payer_name, amount, payment_method]
        );

        // 2. Update the adoption application status to COMPLETED
        await client.query(
            `UPDATE adoption_applications SET status = 'COMPLETED' WHERE application_id = $1`,
            [application_id]
        );

        await client.query('COMMIT'); // Save all changes
        res.status(201).json({ message: "Payment processed successfully!" });

    } catch (err) {
        await client.query('ROLLBACK'); // Discard all changes if any error occurs
        console.error("Transaction error:", err);
        res.status(500).json({ error: "Payment processing failed due to server error." });
    } finally {
        client.release(); // Return client to the pool
    }
};

module.exports = {
    getTransactions,
    processPayment
};
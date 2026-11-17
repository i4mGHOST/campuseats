const express = require('express');
const cors = require('cors');
const pool = require('./db');
const authRoutes = require('./routes/authRoutes');
const petRoutes = require('./routes/petRoutes');
const medicalRoutes = require('./routes/medicalRoutes');
const rescueRoutes = require('./routes/rescueRoutes');
const adoptionRoutes = require('./routes/adoptionRoutes');
const transactionRoutes = require('./routes/transactionRoutes');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

// Mount Auth Routes
app.use('/api/auth', authRoutes);

//Mount Pet Routes
app.use('/api/pets', petRoutes);

//Mount Medical Routes
app.use('/api/medical', medicalRoutes);

//Mount Rescue Routes
app.use('/api/rescue', rescueRoutes);

//Mount Adoption Routes
app.use('/api/adoptions', adoptionRoutes);

//Mount Transaction Routes
app.use('/api/transactions', transactionRoutes);

// Health Endpoint
app.get('/api/health', async (req, res) => {
  try {
    const result = await pool.query('SELECT NOW()');
    res.json({
      status: 'ONLINE',
      message: 'Pawsitive Connect Backend Active',
      databaseTimestamp: result.rows[0].now
    });
  } catch (err) {
    res.status(500).json({ status: 'OFFLINE', error: err.message });
  }
});

app.put('/api/adoptions/:id/status', async (req, res) => {
    const { id } = req.params;
    const { status, fee_amount } = req.body;
    try {
        if (fee_amount) {
            await pool.query('UPDATE adoptions SET status = $1, fee_amount = $2 WHERE id = $3', [status, fee_amount, id]);
        } else {
            await pool.query('UPDATE adoptions SET status = $1 WHERE id = $2', [status, id]);
        }
        res.status(200).json({ message: "Status updated successfully" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// POST /api/rescue/:id/assign
app.post('/api/rescue/:id/assign', async (req, res) => {
    const { id } = req.params;
    const { status, assigned_unit } = req.body;
    try {
        await pool.query('UPDATE rescue_dispatches SET status = $1, assigned_unit = $2 WHERE id = $3', [status, assigned_unit, id]);
        res.status(200).json({ message: "Unit assigned successfully" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  console.log(`Backend server running on http://localhost:${PORT}`);
});
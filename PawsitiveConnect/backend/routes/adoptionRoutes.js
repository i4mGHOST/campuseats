const express = require('express');
const router = express.Router();
const { getAdoptions, updateAdoptionStatus } = require('../controllers/adoptionController');

// GET /api/adoptions
router.get('/', getAdoptions);

// PUT /api/adoptions/:id/status
router.put('/:id/status', updateAdoptionStatus);

module.exports = router;
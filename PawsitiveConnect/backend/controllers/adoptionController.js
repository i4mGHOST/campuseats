const pool = require('../db'); // Adjust path to your database connection file if necessary

// Fetch all adoption applications
const getAdoptions = async (req, res) => {
    try {
        const query = `
            SELECT application_id, applicant_name, pet_name, status, application_date
            FROM adoption_applications
            ORDER BY application_date DESC;
        `;
        const result = await pool.query(query);
        res.status(200).json(result.rows);
    } catch (err) {
        console.error("Error fetching adoption applications:", err);
        res.status(500).json({ error: "Failed to fetch adoption applications" });
    }
};

// PUT: Update application status (Admin Review)
const updateAdoptionStatus = async (req, res) => {
    const { id } = req.params;
    const { status } = req.body;

    try {
        await pool.query(
            `UPDATE adoption_applications SET status = $1 WHERE application_id = $2`,
            [status, id]
        );
        res.status(200).json({ message: "Status updated successfully to " + status });
    } catch (err) {
        console.error("Error updating status:", err);
        res.status(500).json({ error: "Failed to update status" });
    }
};

module.exports = {
    getAdoptions
};

module.exports = {
    getAdoptions,
    updateAdoptionStatus // Export the new function
};

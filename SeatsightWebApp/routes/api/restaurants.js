import express from "express";
import db from "../../config/database.js";

const router = express.Router();

// ✅ Fetch All Restaurants (For Web Admin)
router.get("/", async (req, res) => {
    try {
        const result = await db.query("SELECT id, name, address FROM restaurants");
        res.json(result.rows);
    } catch (err) {
        console.error("Database Error:", err);
        res.status(500).json({ error: "Failed to fetch restaurants" });
    }
});

// ✅ Add a New Restaurant (Owner Only)
router.post("/", async (req, res) => {
    const { owner_id, name, address, ip_camera_url } = req.body;

    try {
        const result = await db.query(
            "INSERT INTO restaurants (owner_id, name, address, ip_camera_url) VALUES ($1, $2, $3, $4) RETURNING id, name",
            [owner_id, name, address, ip_camera_url]
        );
        res.json({ message: "Restaurant added successfully", restaurant: result.rows[0] });
    } catch (err) {
        console.error("Insert Error:", err);
        res.status(500).json({ error: "Failed to add restaurant" });
    }
});

// ✅ Fetch Restaurant by Owner ID (Fix JSON Response)
router.get("/owner/:owner_id", async (req, res) => {
    const { owner_id } = req.params;

    try {
        const result = await db.query(
            "SELECT id, name FROM restaurants WHERE owner_id = $1",
            [owner_id]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({ error: "No restaurant found for this owner." });  // ✅ Correct JSON response
        }

        res.json(result.rows[0]);  // ✅ Always return a JSON object
    } catch (err) {
        console.error("Database error:", err);
        res.status(500).json({ error: "Database error" });
    }
});



export default router;

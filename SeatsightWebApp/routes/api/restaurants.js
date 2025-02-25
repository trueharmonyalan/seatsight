import express from "express";
import db from "../../config/database.js";

const router = express.Router();

// ✅ Fetch All Restaurants (For Web Admin)
router.get("/", async (req, res) => {
    try {
        const result = await db.query("SELECT id, name, address FROM restaurants");
        res.json(result.rows);
    } catch (err) {
        console.error(err);
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
        console.error(err);
        res.status(500).json({ error: "Failed to add restaurant" });
    }
});

export default router;

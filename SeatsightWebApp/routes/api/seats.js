import express from "express";
import db from "../../config/database.js";

const router = express.Router();

// ✅ Get Available Seats for a Restaurant
router.get("/:restaurant_id", async (req, res) => {
    const { restaurant_id } = req.params;

    try {
        const result = await db.query(
            "SELECT id, position_x, position_y, status FROM seats WHERE restaurant_id = $1",
            [restaurant_id]
        );
        res.json(result.rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Failed to fetch seats" });
    }
});

export default router;

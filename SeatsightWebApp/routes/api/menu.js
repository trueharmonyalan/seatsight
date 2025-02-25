import express from "express";
import db from "../../config/database.js";

const router = express.Router();

// ✅ Fetch Menu for a Restaurant
router.get("/:restaurant_id", async (req, res) => {
    const { restaurant_id } = req.params;
    try {
        const result = await db.query("SELECT id, name, description, price FROM menu_items WHERE restaurant_id = $1", [restaurant_id]);
        res.json(result.rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Failed to fetch menu" });
    }
});

// ✅ Add Menu Item
router.post("/", async (req, res) => {
    const { restaurant_id, name, description, price } = req.body;

    try {
        const result = await db.query(
            "INSERT INTO menu_items (restaurant_id, name, description, price) VALUES ($1, $2, $3, $4) RETURNING id, name",
            [restaurant_id, name, description, price]
        );
        res.json({ message: "Menu item added", item: result.rows[0] });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Failed to add menu item" });
    }
});

export default router;

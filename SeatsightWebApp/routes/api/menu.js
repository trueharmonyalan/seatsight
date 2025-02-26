import express from "express";
import db from "../../config/database.js";

const router = express.Router();
router.use(express.json());

// ✅ Fetch Menu for a Restaurant
router.get("/:restaurant_id", async (req, res) => {
    const { restaurant_id } = req.params;
    console.log(`API Request: Fetching menu for restaurant_id = ${restaurant_id}`); // Debugging

    try {
        const result = await db.query(
            "SELECT id, name, description, price FROM menu_items WHERE restaurant_id = $1",
            [restaurant_id]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({ message: "No menu found for this restaurant." });
        }

        res.json(result.rows);
    } catch (err) {
        console.error("API Fetch Error:", err);
        res.status(500).json({ message: "Failed to fetch menu" });
    }
});


router.post("/update", async (req, res) => {
    console.log("Received API request:", req.body);
    const { owner_id, restaurant_id, items, removedItems } = req.body;

    if (!owner_id || !restaurant_id || !Array.isArray(items)) {
        return res.status(400).json({ message: "Invalid request: Missing required fields." });
    }

    try {
        await db.query("BEGIN");

        if (removedItems?.length > 0) {
            await db.query(
                "DELETE FROM menu_items WHERE restaurant_id = $1 AND id = ANY($2)",
                [restaurant_id, removedItems]
            );
        }

        for (const item of items) {
            if (!item.name || !item.price) {
                return res.status(400).json({ message: "Invalid item: name and price are required." });
            }

            if (item.id) {
                await db.query(
                    "UPDATE menu_items SET name = $1, description = $2, price = $3 WHERE id = $4 AND restaurant_id = $5",
                    [item.name, item.description || "", item.price, item.id, restaurant_id]
                );
            } else {
                await db.query(
                    "INSERT INTO menu_items (restaurant_id, name, description, price) VALUES ($1, $2, $3, $4)",
                    [restaurant_id, item.name, item.description || "", item.price]
                );
            }
        }

        await db.query("COMMIT");
        res.json({ message: "Menu updated successfully!" });

    } catch (error) {
        await db.query("ROLLBACK");
        console.error("Submit Error:", error);
        res.status(500).json({ message: "Error updating menu" });
    }
});


export default router;

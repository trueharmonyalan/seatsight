import express from "express";
import db from "../config/database.js";

const router = express.Router();
const currentYear = new Date().getFullYear();
// Authentication middleware
const ensureAuthenticated = (req, res, next) => {
    if (req.isAuthenticated()) return next();
    res.redirect("/");
};
router.use(express.json())
// Get menu page
router.get("/menu", ensureAuthenticated, (req, res) => {
    console.log("User ID:", req.user.id);  // Now logs before rendering
    res.render("update-menu.ejs",{ year: currentYear });
});

// Fetch items
router.get('/fetch-items', ensureAuthenticated, async (req, res) => {
    try {
        const result = await db.query(
            'SELECT id, name, description FROM items WHERE user_id = $1',
            [req.user.id]
        );
        res.json(result.rows);
    } catch (error) {
        console.error("Fetch Error:", error);
        res.status(500).json({ message: 'Error fetching items' });
    }
});

// Handle adding, updating, and removing items
router.post('/submit-items', ensureAuthenticated, async (req, res) => {
    console.log("Received body:", req.body);  

    const { items, removedItems } = req.body;
    
    if (!Array.isArray(items)) {
        return res.status(400).json({ message: "Invalid data: 'items' should be an array." });
    }

    const userId = req.user.id;

    try {
        await db.query("BEGIN");

        if (removedItems?.length > 0) {
            await db.query('DELETE FROM items WHERE user_id = $1 AND id = ANY($2)', [userId, removedItems]);
        }

        for (const item of items) {
            if (item.id) {
                await db.query(
                    'UPDATE items SET name = $1, description = $2 WHERE id = $3 AND user_id = $4',
                    [item.name, item.description, item.id, userId]
                );
            } else {
                const insertResult = await db.query(
                    'INSERT INTO items (user_id, name, description) VALUES ($1, $2, $3) RETURNING id',
                    [userId, item.name, item.description]
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

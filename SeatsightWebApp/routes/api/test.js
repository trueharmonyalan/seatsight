import express from "express";
import argon2 from "argon2";
import db from "../../config/database.js";

const router = express.Router();


    router.get("/tested", async (req, res) => {
        try {
            const result = await db.query("SELECT * FROM owners"); // ✅ Fetch all records
            res.json({ message: "Owners fetched successfully", owners: result.rows });
        } catch (err) {
            console.error(err);
            res.status(500).json({ error: "Failed to fetch owners" });
        }
    });


export default router;

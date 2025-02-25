import express from "express";
import argon2 from "argon2";
import db from "../../config/database.js";

const router = express.Router();

// ✅ Register Customer
router.post("/register", async (req, res) => {
    const { email, password } = req.body;

    try {
        const hashedPassword = await argon2.hash(password);
        const result = await db.query(
            "INSERT INTO customers (email, password) VALUES ($1, $2) RETURNING id, email",
            [email, hashedPassword]
        );
        res.json({ message: "Customer registered successfully", customer: result.rows[0] });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Registration failed" });
    }
});

// ✅ Login Customer
router.post("/login", async (req, res) => {
    const { email, password } = req.body;

    try {
        const result = await db.query("SELECT * FROM customers WHERE email = $1", [email]);

        if (result.rows.length === 0) {
            return res.status(401).json({ error: "User not found" });
        }

        const customer = result.rows[0];

        if (await argon2.verify(customer.password, password)) {
            res.json({ message: "Login successful", customer_id: customer.id, email: customer.email });
        } else {
            res.status(401).json({ error: "Invalid credentials" });
        }
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Login failed" });
    }
});

export default router;

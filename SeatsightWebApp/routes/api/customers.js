import express from "express";
import argon2 from "argon2";
import db from "../../config/database.js";

const router = express.Router();

// ✅ Register Customer (Updated to Use `users` Table)
router.post("/register", async (req, res) => {
    const { email, password } = req.body;

    try {
        const hashedPassword = await argon2.hash(password);
        const result = await db.query(
            "INSERT INTO users (email, password, role) VALUES ($1, $2, 'customer') RETURNING id, email",
            [email, hashedPassword]
        );

        res.json({ message: "Customer registered successfully", customer: result.rows[0] });
    } catch (err) {
        console.error("Registration Error:", err);
        res.status(500).json({ error: "Registration failed" });
    }
});

// ✅ Login Customer (Updated to Use `users` Table)
// router.post("/login", async (req, res) => {
//     const { email, password } = req.body;

//     try {
//         const result = await db.query("SELECT * FROM users WHERE email = $1 AND role = 'customer'", [email]);

//         if (result.rows.length === 0) {
//             return res.status(401).json({ error: "Customer not found" });
//         }

//         const customer = result.rows[0];

//         if (await argon2.verify(customer.password, password)) {
//             res.json({ message: "Login successful", customer_id: customer.id, email: customer.email });
//             res.json()
//         } else {
//             res.status(401).json({ error: "Invalid credentials" });
//         }
//     } catch (err) {
//         console.error("Login Error:", err);
//         res.status(500).json({ error: "Login failed" });
//     }
// });

router.post("/login", async (req, res) => {
    const { email, password } = req.body;

    try {
        const result = await db.query(
            "SELECT * FROM users WHERE email = $1 AND role = 'customer'", 
            [email]
        );

        if (result.rows.length === 0) {
            return res.status(401).json({ error: "Customer not found" });
        }

        const customer = result.rows[0];

        if (await argon2.verify(customer.password, password)) {
            return res.json({ 
                message: "Login successful", 
                customer_id: customer.id,
                user_id: customer.id, // Added user_id
                email: customer.email 
            });
        } else {
            return res.status(401).json({ error: "Invalid credentials" });
        }
    } catch (err) {
        console.error("Login Error:", err);
        return res.status(500).json({ error: "Login failed" });
    }
});

export default router;

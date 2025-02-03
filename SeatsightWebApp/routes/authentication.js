// /routes/auth.js
import express from "express";
const router = express.Router();

router.get("/register", (req, res) => {
  res.render("register.ejs");
});

router.get("/login", (req, res) => {
  res.render("login.ejs");
});

export default router;

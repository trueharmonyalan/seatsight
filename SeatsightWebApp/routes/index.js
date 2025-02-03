// /routes/index.js
import express from "express";
const router = express.Router();


const currentYear = new Date().getFullYear();

router.get("/", (req, res) => {
  res.render("index.ejs", { year: currentYear });
});

router.get("/register",(req,res)=>{
    res.render("register.ejs")
})

router.get("/login",(req,res)=>{
    res.render("login.ejs")
})

export default router;

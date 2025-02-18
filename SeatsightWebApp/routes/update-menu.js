// /routes/auth.js
import express from "express";
const router = express.Router();

const currentYear = new Date().getFullYear()

router.get("/menu",(req,res)=>{
    if(req.isAuthenticated()){
        res.render("booked-seat-info.ejs")
    } else{
        res.redirect("/")
    }
    
})







export default router;

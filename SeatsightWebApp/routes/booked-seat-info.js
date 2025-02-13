// /routes/auth.js
import express from "express";
const router = express.Router();


router.get("/bookedInfo",(req,res)=>{
    res.render("booked-seat-info.ejs")
})







export default router;

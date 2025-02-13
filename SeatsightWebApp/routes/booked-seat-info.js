// /routes/auth.js
import express from "express";
const router = express.Router();

const currentYear = new Date().getFullYear()

router.get("/bookedInfo",(req,res)=>{
    if(req.isAuthenticated()){
        res.render("booked-seat-info.ejs",{
            year: currentYear,
            info : {
            totalSeats:4,
            bookedSeats:1
            }
            
        })
    }
    
})







export default router;

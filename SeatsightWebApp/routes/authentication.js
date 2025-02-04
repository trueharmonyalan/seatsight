// /routes/auth.js
import express from "express";
const router = express.Router();
import argon2 from "argon2"
import pg from "pg"

// file imports
import db from "../config/database.js";
import passport from "passport";




router.get("/auth",(req,res)=>{
  if(req.isAuthenticated()){
    res.render("register.ejs")
  }else{
    res.redirect("/")
  }
})




router.post("/register", async (req, res) => {
  const email = req.body.username
  const password = req.body.password

  try{

    const checkEmail = await db.query("select * from users where email = $1",[email])

    if(checkEmail.rows.length>0){
      res.render("register.ejs",{
          potentialError:{
            emailError: "This Email already registered with us.",
          }
      })
      
    }else{

        const hash = await argon2.hash(password)
        await db.query("insert into users(email,password) values ($1,$2)",[email,hash])
        res.send("authenticated")
     
    }

  }catch(err){
    console.log(err)
  }



});




router.post("/login",passport.authenticate("local",{
  successRedirect: "/auth",
  failureRedirect: "login"

}));






export default router;

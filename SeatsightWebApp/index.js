import express from "express"
import passport from "passport"
import session from "express-session"
import {Strategy} from "passport-local"
import bcrypt from "bcrypt"
import pg from "pg"
import env from "dotenv"



const app = express()
const port = 3000
env.config()

const db = new pg.Client({
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    host: process.env.HOST,
    database: process.env.DB_DATABASE,
    port: process.env.PORT
})







app.use(express.static('public'));
app.use(express.urlencoded({extended:true}))


app.get("/",(req,res)=>{
    res.render("index.ejs")
})

app.get("/register",(req,res)=>{
    res.render("register.ejs")
})

app.get("/login",(req,res)=>{
    res.render("login.ejs")
})




app.listen(port,()=>{
    console.log(`server is running on port ${port}`)
})
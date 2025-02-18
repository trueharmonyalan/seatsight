// /routes/auth.js
import express from "express";
const router = express.Router();
import argon2 from "argon2";
import pg from "pg";

// file imports
import db from "../config/database.js";
import passport from "passport";

const currentYear = new Date().getFullYear();

console.log();
router.get("/auth", (req, res) => {
  if (req.isAuthenticated()) {
    res.render("home.ejs", {
      year: currentYear,
      state: true,
    });
  } else {
    res.redirect("/");
  }
});

router.get("/logout", (req, res) => {
  req.logout((err) => {
    if (err) {
      return err;
    }
    res.redirect("/");
  });
});

router.post("/register", async (req, res) => {
  const email = req.body.username;
  const password = req.body.password;

  console.log(email);
  console.log(password);

  if (!req.body.username) {
    res.render("register.ejs", {
      potentialError: {
        noemailError: "Provide your email.",
      },
    });
  }

  if (!req.body.password) {
    res.render("register.ejs", {
      potentialError: {
        noPasswordError: "Provide your password.",
      },
    });
  }

  try {
    const checkEmail = await db.query("select * from users where username = $1", [
      email,
    ]);

    if (checkEmail.rows.length > 0) {
      res.render("register.ejs", {
        potentialError: {
          emailError: "This email is already registered with us.",
        },
      });
    } else {
      const hash = await argon2.hash(password);
      const result = await db.query(
        "insert into users(username,password) values ($1,$2) RETURNING *",
        [email, hash]
      );

      const user = result.rows[0];

      req.login(user, (err) => {
        console.log(err);
        res.redirect("/auth");
      });
    }
  } catch (err) {
    console.log(err);
  }
});

router.post(
  "/login",
  passport.authenticate("local", {
    successRedirect: "/auth",
    failureRedirect: "login",
  })
);

export default router;

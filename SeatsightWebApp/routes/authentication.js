// /routes/auth.js
import express from "express";
const router = express.Router();
import argon2 from "argon2";
import fetch from "node-fetch"; 


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

// router.post("/register", async (req, res) => {
//   const email = req.body.username;
//   const password = req.body.password;

//   console.log(email);
//   console.log(password);

//   if (!req.body.username) {
//     res.render("register.ejs", {
//       potentialError: {
//         noemailError: "Provide your email.",
//       },
//     });
//   }

//   if (!req.body.password) {
//     res.render("register.ejs", {
//       potentialError: {
//         noPasswordError: "Provide your password.",
//       },
//     });
//   }

//   try {
//     const checkEmail = await db.query("select * from users where username = $1", [
//       email,
//     ]);

//     if (checkEmail.rows.length > 0) {
//       res.render("register.ejs", {
//         potentialError: {
//           emailError: "This email is already registered with us.",
//         },
//       });
//     } else {
//       const hash = await argon2.hash(password);
//       const result = await db.query(
//         "insert into users(username,password) values ($1,$2) RETURNING *",
//         [email, hash]
//       );

//       const user = result.rows[0];

//       req.login(user, (err) => {
//         console.log(err);
//         res.redirect("/auth");
//       });
//     }
//   } catch (err) {
//     console.log(err);
//   }
// });


router.post("/register", async (req, res) => {
  const email = req.body.username;
  const password = req.body.password;

  if (!email) {
    return res.render("register.ejs", {
      potentialError: { noemailError: "Provide your email." },
      year:currentYear
    });
  }

  if (!password) {
    return res.render("register.ejs", {
      potentialError: { noPasswordError: "Provide your password." },
      year:currentYear 
    });
  }

  try {
    const response = await fetch("http://localhost:3001/api/owners/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    const data = await response.json();

    if (response.ok) {
      req.login(data.owner, (err) => {
        if (err) console.log(err);
        res.redirect("/auth");
      });
    } else if (response.status === 409) {
      // ✅ Handle "Email already registered" case
      res.render("register.ejs", {
        potentialError: { emailError: "This email is already registered. Please log in instead." },
        year:currentYear 
      });
    } else {
      res.render("register.ejs", {
        potentialError: { emailError: "This email is already registered. Please log in instead." ||data.error  },
        year:currentYear 
      });
    }
  } catch (err) {
    console.log(err);
    res.render("register.ejs", {
      potentialError: { emailError: "Server error. Try again later." },
      year:currentYear 
    });
  }
});


router.post("/login", (req, res, next) => {
  passport.authenticate("local", (err, user, info) => {
    if (err) {
      console.log("Auth: Passport error:", err);
      return res.render("login.ejs", { potentialError: { loginError: "Server error. Try again later." },
      year:currentYear });
    }

    if (!user) {
      console.log("Auth: Login failed -", info.message);
      return res.render("login.ejs", { potentialError: { loginError: info.message || "Invalid credentials." },
      year:currentYear});
    }

    req.logIn(user, (err) => {
      if (err) {
        console.log("Auth: Error during session login:", err);
        return res.render("login.ejs", { potentialError: { loginError: "Login failed." },
        year:currentYear});
      }

      console.log("Auth: Login successful - User session created:", req.session);
      res.redirect("/auth");
    });
  })(req, res, next);
});


export default router;

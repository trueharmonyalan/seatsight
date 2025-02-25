import express from "express";
const router = express.Router();
import argon2 from "argon2";
import fetch from "node-fetch";
import passport from "passport";

const currentYear = new Date().getFullYear();

router.get("/auth", (req, res) => {
    if (req.isAuthenticated()) {
        res.render("home.ejs", { year: currentYear, state: true });
    } else {
        res.redirect("/");
    }
});

router.get("/logout", (req, res) => {
    req.logout((err) => {
        if (err) return console.error("Logout Error:", err);
        res.redirect("/");
    });
});

router.post("/register", async (req, res) => {
  const { restaurantName, username: email, password } = req.body;

  if (!restaurantName) {
      return res.render("register.ejs", {
          potentialError: { noRestaurantName: "Enter restaurant name." },
          year: currentYear
      });
  }

  if (!email) {
      return res.render("register.ejs", {
          potentialError: { noemailError: "Provide your email." },
          year: currentYear
      });
  }

  if (!password) {
      return res.render("register.ejs", {
          potentialError: { noPasswordError: "Provide your password." },
          year: currentYear
      });
  }

  try {
      const response = await fetch("http://localhost:3001/api/owners/register", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ restaurantName, email, password }),
      });

      const data = await response.json();

      if (response.ok) {
          req.login(data.user, (err) => {
              if (err) {
                  console.error("Auth: Error during session login:", err);
                  return res.render("register.ejs", {
                      potentialError: { emailError: "Registration successful, but auto-login failed. Please log in manually." },
                      year: currentYear
                  });
              }
              console.log("Auth: Registration successful. Redirecting to /auth");
              res.redirect("/auth");
          });
      } else if (response.status === 409) {
          res.render("register.ejs", {
              potentialError: { emailError: "This email is already registered. Please log in instead." },
              year: currentYear
          });
      } else {
          res.render("register.ejs", {
              potentialError: { emailError: data.error || "Registration failed." },
              year: currentYear
          });
      }
  } catch (err) {
      console.error("Registration Error:", err);
      res.render("register.ejs", {
          potentialError: { emailError: "Server error. Try again later." },
          year: currentYear
      });
  }
});
router.post("/login", (req, res, next) => {
  passport.authenticate("local", (err, user, info) => {
    console.log("Auth Debug: Passport returned ->", { err, user, info });

    if (err) {
      console.error("Auth: Passport error:", err);
      return res.render("login.ejs", {
        potentialError: { loginError: "Server error. Try again later." },
        year: currentYear
      });
    }

    if (!user) {
      console.log("Auth: Login failed -", info?.message || "No user returned");
      return res.render("login.ejs", {
        potentialError: { loginError: info?.message || "Invalid credentials." },
        year: currentYear
      });
    }

    req.logIn(user, (err) => {
      if (err) {
        console.error("Auth: Error during session login:", err);
        return res.render("login.ejs", {
          potentialError: { loginError: "Login failed." },
          year: currentYear
        });
      }

      console.log("Auth: Login successful - User session created:", req.session);
      res.redirect("/auth");
    });
  })(req, res, next);
});



export default router;

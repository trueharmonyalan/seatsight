import express from "express";
import passport from "passport";

const router = express.Router();

const currentYear = new Date().getFullYear();

router.get("/settings", (req, res) => {
  if (req.isAuthenticated()) {
    res.render("homepage-settings-card.ejs", 
        {
      year: currentYear,
    }
);
  } else {
    res.redirect("/");
  }
});

router.post("/settings", (req, res) => {
  const url = req.body.ipurl;
  console.log(url);

  var urlActive = false;
  if (url) {
    urlActive = true;
  }

  res.render("homepage-settings-card.ejs", {
    info: {
      showFromUrlState: urlActive,
      showUrl: url,
    },
  });
});

export default router;

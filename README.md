![Seatsight Logo](SeatsightWebApp/public/img/seatsight.png)
# Seatsight
## Seatsight is a system that is designed to identify the available seats in restaurants. 
This system contain three technologies
- Web App : developed using ExpresJs, NodeJs and EJS
- Deep Learning model : Custom trained yolo model
- Application For Mobile : Native android app developed using Kotlin

The sytem is interconnected with RESTapi and FastApi. Postgres is used as the database.

## How its work
If the user(android app) find the restaurant to check there seat status, when user clicks the view button, it the post the restaurant_id to the deep learning model by using restAPI
then that result is passed to the fastAPI that is connected with database, check the restaurant_id with the postgres database and fetch its ip_url that is updated by the restaurant owner (web app)

if that is vaild ip_url, it try to fetch the video feed and using openCV we convert the video feed into individual frame, then the frame is processed in yolo to get the prediction about the
seat status.


## how to Run

First we need to startup the system, inorder to start we need to run three files
- Express server: file in SeatSightWebapp, named server.js
  ```
  npx nodemon server.js
  ```
- RestApi server: file in SeatSightWebapp/routes/api, file nammed startup.js
  ```
  npx nodemon startup.js
  ```
- Deep learning server: file in SeatSightModel, file named main.py
  ```
  python3 main.py
  ```

Before runing the system, it is requried to install the recommend packages that is used in these systems. After cloning this project you need to run
  ```
  npm i
  ```
Install packages for SeatsightWebapp
  ```
  cd seatsight
  ```
  ```
  cd seatsight/SeatsightWebapp
  ```
  ```
  npm i
  ```
  ```
  npx nodemon server.js
  ```
Install packages for Api server
 ```
  cd seatsight
  ```
  ```
  cd seatsight/SeatsightWebapp/routes/api
  ```
  ```
  npm i
  ```
  ```
  npx nodemon startup.js
  ```
For deeplearning model, is better before you run this main.py, try to create a venv then install the packages
inside the venv
  ```
 pip install ultralytics
  ```
  ```
  cd seatsight/seatsightModel
  ```
  ```
  python3 main.py
  ```



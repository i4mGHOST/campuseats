# **CS543 Web Services**
## **Assignment 8: Converting a Service into a REST API**  
### **IIIT Vadodara**

## Team Details

| Serial Number | Team Member Name | Roll Number |
| :--- | :--- | :--- |
| 1 | SUVOJIT NAG|20251651094 |
| 2 | VANSH PANWAR|20251651101 |
| 3 | AJEET SHAKYA|20251651010 |
| 4 | ANKIT|20251651021 |
| 5 | ANJUL YADAV| 20251651020|

---

##  Project Overview

This project implements a lightweight 3-tier REST API for the **CampusEats menu/catalogue** service built using Node.js and the Express framework. The client application consists of a plain HTML/CSS front-end that communicates asynchronously via the native Web `fetch()` API to retrieve the current menu array and submit new food items without page reloads.

---

##  Tech Stack & Requirements :-

* **Runtime Environment:** Node.js (v18+)
* **Backend Framework:** Express.js (`^4.19.2`)
* **Frontend:** Plain HTML5, CSS3, Vanilla JavaScript (ES6+ async/await)
* **Architecture:** 3-Tier Decoupled Architecture (Client $\rightarrow$ REST API Server $\rightarrow$ In-Memory Data Store)

---

##  Sub-repository Structure :-

```text
assignment_8_campus_eats_menu_catalogue/
├── server.js          # Express REST API routes, handlers, and in-memory data
├── package.json       # Node package manager configuration & dependencies
├── README.md          # Project documentation and assignment answers
└── public/
    ├── index.html     # Client interface & fetch script
    └── style.css      # UI stylesheet
```

---


##  Assignment Technical Questions & Answers :-

### 1. Which CampusEats service did you pick, and what are its routes (method + path) ?

We selected the **Menu / Catalogue** service for CampusEats. The implemented REST routes are:

1. **`GET /items`**: Retrieves the complete list of menu items from the in-memory array.
2. **`GET /items/:id`**: Fetches details for a single menu item matching the URL path parameter `id`.
3. **`POST /items`**: Validates incoming payload data and appends a new menu dish to the items array.

---

### 2. Which status codes did you return, and for which outcomes (200/201/404/422) ?

* **`200 OK`**: Returned for successful read requests (`GET /items` and `GET /items/:id`) when data is found and returned to the client.
* **`201 Created`**: Returned by `POST /items` upon successful validation and creation of a new menu dish item.
* **`404 Not Found`**: Returned by `GET /items/:id` when the requested `id` does not match any existing record in the menu array.
* **`422 Unprocessable Entity`**: Returned by `POST /items` when request validation fails due to a missing required field (specifically, omitting `name`).

---

### 3. Where did you use `req.params` and where `req.body` ? Why does POST need `express.json()` ?

* **`req.params`**: Used in the `GET /items/:id` route handler as `req.params.id`. It extracts dynamic wildcard variables embedded directly in the HTTP request URL path.
* **`req.body`**: Used in the `POST /items` route handler as `req.body.name`, `req.body.price`, etc. It accesses data submitted inside the HTTP payload body sent by the client form.
* **Why POST needs `express.json()`**: Node.js and Express stream incoming HTTP request bodies as raw binary data by default. The `express.json()` built-in middleware intercept incoming requests carrying a `Content-Type: application/json` header and parses the raw binary payload into a native JavaScript object, populating `req.body`. Without registering `app.use(express.json())`, `req.body` will evaluate to `undefined`, causing validation script failures on POST operations.

---

## How to Run:-
1. Clone the repository to your local directory and navigate to the folder `assignment_8_campuseats_menu_catalogue`.
2. Ensure Node framework is installed (Pre-requisites)
3. Install dependencies: `npm install express`
4. Start the server: `node server.js`
5. For checking api request/fetch to database updations browse Apollo Sandbox at `http://localhost:3000`.

---
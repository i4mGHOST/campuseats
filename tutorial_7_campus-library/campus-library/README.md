# Campus Library — GraphQL & MySQL Web Application

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
The **Campus Library** project is a full-stack web application designed for managing authors and their corresponding books. It demonstrates the integration of a **GraphQL API layer** between a **MySQL relational database** and a plain **HTML/CSS/JavaScript front-end**. 

Rather than relying on traditional REST endpoints, the application leverages GraphQL to query nested relational data (authors and their books) in a single request and execute mutations to manipulate database records.

---

##  System Architecture

The project follows a decoupled 3-tier architecture:

```
+-----------------------------------+
|       Front-End Interface         |
|   (HTML5 / CSS3 / Vanilla JS)     |
+-----------------------------------+
                  |
                  | HTTP POST (GraphQL Queries & Mutations)
                  v
+-----------------------------------+
|         GraphQL API Server        |
|     (Node.js + Apollo Server)     |
+-----------------------------------+
                  |
                  | SQL Queries (mysql2 pool)
                  v
+-----------------------------------+
|         Relational Database       |
|              (MySQL)              |
+-----------------------------------+
```

---

##  Core Functionality

### 1. Data Retrieval (Query)
* **Author & Book Listing:** Fetches all authors along with their associated books, titles, and publication years in a single GraphQL query payload.
* **Year Filtering:** Supports filtering books dynamically by publication year using input parameters passed directly to the GraphQL `books(year: Int)` query.

### 2. Data Manipulation (Mutations)
* **Add Book:** Inserts a new book record linked to a specific author ID directly into the MySQL database via the `addBook` mutation.
* **Delete Book:** Removes a book record from the MySQL database by its unique identifier using the `deleteBook` mutation.

---

## 📂 File Contents & Structure

```
campus-library/
├── schema.sql         # SQL script defining database tables (authors, books) and seed data
├── db.js              # MySQL connection pool configuration using mysql2/promise
├── server.js          # Main entry point; defines GraphQL schema (typeDefs), resolvers, and Apollo Server instance
├── package.json       # Node.js project configuration and package dependencies
├── public/
│   ├── index.html     # Front-end UI containing HTML structure and vanilla JS fetch logic
│   └── style.css      # Custom CSS styles for layout, cards, and input controls
└── README.md          # Project documentation
```

### Key Components

* **`schema.sql`**: Configures the `gqldemo` database and establishes the 1-to-many relationship (`FOREIGN KEY (author_id) REFERENCES authors(id)`).
* **`db.js`**: Exported MySQL connection pool enabling asynchronous database operations via promises.
* **`server.js`**: Configures Apollo Server. Contains GraphQL type definitions (`Author`, `Book`, `Query`, `Mutation`) and resolver functions translating GraphQL requests into parameterized SQL queries.
* **`public/index.html`**: Sends GraphQL request payloads via standard `fetch()` POST operations and dynamically renders the DOM based on response JSON.
* **`public/style.css`**: Provides styling for author cards, form elements, filter bars, and action buttons.
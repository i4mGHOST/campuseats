# CampusEats - Menu Service (Assignment 7 - Implementing GraphQL)

## Team Details

| Serial Number | Team Member Name | Roll Number |
| :--- | :--- | :--- |
| 1 | SUVOJIT NAG|20251651094 |
| 2 | VANSH PANWAR|20251651101 |
| 3 | AJEET SHAKYA|20251651010 |
| 4 | ANKIT|20251651021 |
| 5 | ANJUL YADAV| 20251651020|

---

## Answers to Assignment Questions:-

### 1. Which CampusEats service did you pick?
We picked the **Menu / Dishes Service**, which manages food items offered on CampusEats, including their item names, prices, and availability status.

### 2. Name one field a client can skip and one it cannot (why we use the "!" sign).
- **A field a client can skip:** Optional query arguments or filter fields (if configured without the non-null marker `!`) can be omitted by the client during requests.
- **A field a client CANNOT skip:** The `name: String!`, `price: Float!`, and `available: Boolean!` arguments in our `upsertDish` mutation require the `!` (non-null modifier). This enforces strict type validation at the GraphQL engine layer—if a client omits any of these required fields, the request is rejected immediately before reaching the database resolver.

### 3. Map it to SQL/other database: which part of your query became the WHERE, and which resolver ran the INSERT?
- **WHERE mapping:** When querying an individual item by its identifier via `dish(id: ID!)`, the GraphQL argument `{ id }` maps directly into the SQL lookup clause: `SELECT * FROM dishes WHERE id = ?`. Furthermore, our upsert mutation utilizes a `WHERE` or unique constraint lookup to verify existing records.
- **The INSERT/UPDATE (Upsert) resolver:** The `Mutation.upsertDish` resolver executes an SQL **Upsert** command (`INSERT INTO dishes ... ON DUPLICATE KEY UPDATE ...`), which checks the unique `name` column. If the dish already exists, it updates its price and availability; otherwise, it inserts a new row, and subsequently fetches and returns the record.

---

## How to Run:-
1. Clone the repository to your local directory and navigate to the folder `assignment_7_campuseats_menu`.
2. Ensure MySQL is running and execute `schema.sql`.
3. Update your credentials and port configuration in `db.js`.
4. Install dependencies: `npm install @apollo/server graphql mysql2`
5. Start the GraphQL server: `node server.js`
6. Open `public/index.html` using VS Code Live Server (or your browser) at `http://127.0.0.1:5500`.
7. For checking api request/fetch to database updations browse Apollo Sandbox at `http://localhost:4000`.
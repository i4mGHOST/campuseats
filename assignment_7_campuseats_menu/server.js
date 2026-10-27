// server.js - CampusEats Menu Service GraphQL API with Upsert Logic
const { ApolloServer } = require('@apollo/server');
const { startStandaloneServer } = require('@apollo/server/standalone');
const fs = require('fs');
const path = require('path');
const pool = require('./db');

// Read schema definition from schema.graphql
const typeDefs = fs.readFileSync(path.join(__dirname, 'schema.graphql'), 'utf8');

// Resolvers mapping queries and mutations to MySQL operations
const resolvers = {
  Query: {
    dishes: async () => {
      const [rows] = await pool.query('SELECT * FROM dishes');
      return rows;
    },
    dish: async (_parent, { id }) => {
      const [rows] = await pool.query('SELECT * FROM dishes WHERE id = ?', [id]);
      return rows[0];
    },
  },

  Mutation: {
    // Upsert resolver: Inserts if new, updates price & availability if name exists
    upsertDish: async (_parent, { name, price, available }) => {
      await pool.query(
        `INSERT INTO dishes (name, price, available) VALUES (?, ?, ?) 
         ON DUPLICATE KEY UPDATE price = ?, available = ?`,
        [name, price, available, price, available]
      );
      
      // Fetch and return the updated or newly inserted row by name
      const [rows] = await pool.query('SELECT * FROM dishes WHERE name = ?', [name]);
      return rows[0];
    },
  },
};

// Start standalone Apollo Server on port 4000
async function start() {
  const server = new ApolloServer({ typeDefs, resolvers });
  const { url } = await startStandaloneServer(server, { listen: { port: 4000 } });
  console.log(`CampusEats Menu API ready at ${url}`);
}

start();
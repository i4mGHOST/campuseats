const express = require('express');
const app = express();

// Middleware
app.use(express.json());          // Parses incoming JSON request bodies (req.body)
app.use(express.static('public')); // Serves your HTML/CSS front-end automatically

// In-memory source of truth (seed records)
let items = [
  { id: 1, name: 'Veg Thali', price: 90, available: true },
  { id: 2, name: 'Paneer Roll', price: 70, available: true },
  { id: 3, name: 'Chicken Biryani', price: 120, available: true }
];

let nextId = 4;

// 1. GET /items -- List the whole menu
app.get('/items', (req, res) => {
  res.status(200).json(items);
});

// 2. GET /items/:id -- Get one dish, or return 404 if absent
app.get('/items/:id', (req, res) => {
  const item = items.find(i => i.id === Number(req.params.id));
  if (!item) {
    return res.status(404).json({ error: 'not found' });
  }
  res.status(200).json(item);
});

// 3. POST /items -- Create a new dish with validation (422 if missing name)
app.post('/items', (req, res) => {
  const { name, price, available } = req.body;
  
  if (!name) {
    return res.status(422).json({ error: 'name required' });
  }

  const newItem = {
    id: nextId++,
    name,
    price: Number(price) || 0,
    available: available !== undefined ? Boolean(available) : true
  };

  items.push(newItem);
  res.status(201).json(newItem); // 201 Created
});

// Start the server on port 3000
app.listen(3000, () => {
  console.log('CampusEats REST API running on http://localhost:3000');
});
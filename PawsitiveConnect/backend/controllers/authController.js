const pool = require('../db');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'pawsitive_jwt_secret_key_2026';

// 1. User Registration
exports.register = async (req, res) => {
  const { full_name, email, password, phone, role_id } = req.body;
  const userRole = role_id || 4; // Default to Adopter (role_id = 4)

  try {
    // Check for existing user
    const userCheck = await pool.query('SELECT * FROM users WHERE email = $1', [email]);
    if (userCheck.rows.length > 0) {
      return res.status(400).json({ success: false, message: 'Email is already registered.' });
    }

    // Hash password
    const salt = await bcrypt.genSalt(10);
    const passwordHash = await bcrypt.hash(password, salt);

    // Insert user into PostgreSQL
    const newUser = await pool.query(
      `INSERT INTO users (full_name, email, password_hash, phone, role_id, created_at)
       VALUES ($1, $2, $3, $4, $5, NOW())
       RETURNING user_id, full_name, email, role_id, created_at`,
      [full_name, email, passwordHash, phone, userRole]
    );

    res.status(201).json({
      success: true,
      message: 'User registered successfully.',
      user: newUser.rows[0]
    });
  } catch (error) {
    console.error('Registration Error:', error);
    res.status(500).json({ success: false, message: 'Server error during registration.' });
  }
};

// 2. User Login
exports.login = async (req, res) => {
  const { email, password } = req.body;

  try {
    // Find user
    const result = await pool.query('SELECT * FROM users WHERE email = $1', [email]);
    if (result.rows.length === 0) {
      return res.status(401).json({ success: false, message: 'Invalid email or password.' });
    }

    const user = result.rows[0];

    // Verify password hash
    const isMatch = await bcrypt.compare(password, user.password_hash);
    if (!isMatch) {
      return res.status(401).json({ success: false, message: 'Invalid email or password.' });
    }

    // Sign JWT token
    const token = jwt.sign(
      { userId: user.user_id, roleId: user.role_id, email: user.email },
      JWT_SECRET,
      { expiresIn: '8h' }
    );

    res.status(200).json({
      success: true,
      message: 'Login successful.',
      token,
      user: {
        user_id: user.user_id,
        full_name: user.full_name,
        email: user.email,
        role_id: user.role_id
      }
    });
  } catch (error) {
    console.error('Login Error:', error);
    res.status(500).json({ success: false, message: 'Server error during login.' });
  }
};
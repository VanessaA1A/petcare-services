require('dotenv').config();
const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');
const usersRouter = require('./routes/users');
const authRouter = require('./routes/auth');
const db = require('./db');

const app = express();
const port = process.env.PORT || 3000;

app.use(bodyParser.json());
app.use(express.static(path.join(__dirname, '../public')));

app.get('/', (req, res) => res.sendFile(path.join(__dirname, '../public/login.html')));
app.get('/login', (req, res) => res.sendFile(path.join(__dirname, '../public/login.html')));
app.get('/register', (req, res) => res.sendFile(path.join(__dirname, '../public/register.html')));

app.get('/health', (req, res) => res.json({ status: 'ok' }));

app.use('/api/users', usersRouter);
app.use('/api/auth', authRouter);

async function start() {
  try {
    console.log('Checking database connectivity...');
    const r = await db.query('SELECT 1 as ok');
    if (!r || !r.rows) throw new Error('No response from DB');
    console.log('Database OK:', r.rows[0]);
    app.listen(port, () => {
      console.log(`PetCare services listening on port ${port}`);
    });
  } catch (err) {
    console.error('Failed to start application. DB connectivity error:', err.message || err);
    process.exit(1);
  }
}

start();

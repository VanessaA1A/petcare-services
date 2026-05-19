import 'dotenv/config';
import express from 'express';
import path from 'path';
import bodyParser from 'body-parser';
import usersRouter from './routes/users';
import authRouter from './routes/auth';
import sessionAuth from './middleware/sessionAuth';
import * as authController from './controllers/authController';
import * as usersController from './controllers/usersController';
import { query } from './db';

const app = express();
const port = process.env.PORT || 3000;

app.use(bodyParser.json());
app.use(express.static(path.join(__dirname, '../public')));

app.get('/', (req, res) => res.sendFile(path.join(__dirname, '../public/login.html')));
app.get('/login', (req, res) => res.sendFile(path.join(__dirname, '../public/login.html')));
app.post('/login', authController.login);
app.get('/register', (req, res) => res.sendFile(path.join(__dirname, '../public/register.html')));
app.post('/register', usersController.createUser);
app.get('/profile', sessionAuth, authController.me);

app.get('/health', (req, res) => res.json({ status: 'ok' }));

app.use('/api/users', usersRouter);
app.use('/api/auth', authRouter);

async function start() {
  try {
    console.log('Checking database connectivity...');
    const r = await query('SELECT 1 as ok');
    if (!r || !r.rows) throw new Error('No response from DB');
    console.log('Database OK:', r.rows[0]);
    app.listen(port, () => {
      console.log(`PetCare services listening on port ${port}`);
    });
  } catch (err: any) {
    console.error('Failed to start application. DB connectivity error:', err.message || err);
    process.exit(1);
  }
}

start();

import express from 'express';
const router = express.Router();

import * as auth from '../controllers/authController';
import sessionAuth from '../middleware/sessionAuth';

router.post('/login', auth.login);
router.post('/recover', auth.recoverPassword);
router.get('/me', sessionAuth, auth.me);

export default router;

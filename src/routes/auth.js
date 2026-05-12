const express = require('express');
const router = express.Router();

const auth = require('../controllers/authController');
const sessionAuth = require('../middleware/sessionAuth');

router.post('/login', auth.login);
router.post('/recover', auth.recoverPassword);
router.get('/me', sessionAuth, auth.me);

module.exports = router;

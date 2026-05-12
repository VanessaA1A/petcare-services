const express = require('express');
const router = express.Router();
const users = require('../controllers/usersController');

router.post('/', users.createUser);
router.get('/', users.getAllUsers);
router.get('/:id', users.getUserById);
router.put('/:id', users.updateUser);
router.delete('/:id', users.deleteUser);
router.post('/:id/roles', users.assignRoles);

module.exports = router;

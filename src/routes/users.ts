import express from 'express';
const router = express.Router();
import * as users from '../controllers/usersController';

router.post('/', users.createUser);
router.get('/', users.getAllUsers);
router.get('/:id', users.getUserById);
router.put('/:id', users.updateUser);
router.delete('/:id', users.deleteUser);
router.post('/:id/roles', users.assignRoles);

export default router;

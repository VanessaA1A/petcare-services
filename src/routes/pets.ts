import express from 'express';
import * as pets from '../controllers/petsController';

const router = express.Router();

// Get all pets (admin only)
router.get('/all', pets.getAllPets);

// Get pets for a specific owner
router.get('/owner/:owner_id', pets.getPetsByOwnerId);

// Create a single new pet
router.post('/', pets.createPet);

// Create multiple pets for one owner
router.post('/bulk', pets.createPetsBulk);

// Get a specific pet
router.get('/:id', pets.getPetById);

// Update a pet
router.put('/:id', pets.updatePet);

// Delete a pet
router.delete('/:id', pets.deletePet);

export default router;

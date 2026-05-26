import { Request, Response } from 'express';
import { query, pool } from '../db';
import { PetModel } from '../types/models';
import { PetViewModel } from '../types/viewmodels';
import { v4 as uuidv4 } from 'uuid';

function formatPet(pet: PetModel): PetViewModel {
  return pet as PetViewModel;
}

function validatePetInput(input: any): string[] {
  const errors: string[] = [];
  const ownerId = input.owner_id;
  if (ownerId == null || (typeof ownerId !== 'string' && typeof ownerId !== 'number') || String(ownerId).trim() === '') {
    errors.push('owner_id is required');
  }
  if (!input.name || typeof input.name !== 'string' || !input.name.trim()) {
    errors.push('name is required');
  }
  if (!input.breed || typeof input.breed !== 'string' || !input.breed.trim()) {
    errors.push('breed is required');
  }
  if (!input.size || typeof input.size !== 'string' || !input.size.trim()) {
    errors.push('size is required');
  }
  return errors;
}

function validatePetsArray(pets: any[] | undefined): string[] {
  const errors: string[] = [];
  if (!Array.isArray(pets) || pets.length === 0) {
    errors.push('pets array is required and must contain at least one pet');
    return errors;
  }

  pets.forEach((pet, index) => {
    const petErrors = validatePetInput({ ...pet, owner_id: 'dummy-owner-id' }).filter((err) => err !== 'owner_id is required');
    if (petErrors.length > 0) {
      errors.push(`pet[${index}]: ${petErrors.join(', ')}`);
    }
  });
  return errors;
}

/**
 * Get all pets for a specific owner
 * GET /api/pets/owner/:owner_id
 */
export async function getPetsByOwnerId(req: Request, res: Response) {
  try {
    const { owner_id } = req.params;
    if (!owner_id) return res.status(400).json({ error: 'owner_id is required' });

    const text = 'SELECT id, owner_id, name, species, breed, size, age, weight, description, created_at, updated_at FROM pets WHERE owner_id = $1 ORDER BY created_at DESC';
    const result = await query<PetModel>(text, [owner_id]);
    const pets = result.rows.map((pet) => formatPet(pet));
    res.json(pets);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error fetching pets' });
  }
}

/**
 * Get a specific pet by ID
 * GET /api/pets/:id
 */
export async function getPetById(req: Request, res: Response) {
  try {
    const { id } = req.params;
    if (!id) return res.status(400).json({ error: 'id is required' });

    const text = 'SELECT id, owner_id, name, species, breed, size, age, weight, description, created_at, updated_at FROM pets WHERE id = $1';
    const result = await query<PetModel>(text, [id]);
    if (result.rowCount === 0) return res.status(404).json({ error: 'Pet not found' });

    const pet = result.rows[0];
    res.json(formatPet(pet));
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error fetching pet' });
  }
}

/**
 * Create a new pet for an owner
 * POST /api/pets
 */
export async function createPet(req: Request, res: Response) {
  try {
    const { owner_id, name, species, breed, size, age, weight, description } = req.body as {
      owner_id?: string | number;
      name?: string;
      species?: string;
      breed?: string;
      size?: string;
      age?: number;
      weight?: number;
      description?: string;
    };

    const errors = validatePetInput(req.body);
    if (errors.length > 0) {
      return res.status(400).json({ error: errors.join(', ') });
    }

    const id = uuidv4();
    const text = `INSERT INTO pets (id, owner_id, name, species, breed, size, age, weight, description, created_at, updated_at)
                  VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, NOW(), NOW())
                  RETURNING id, owner_id, name, species, breed, size, age, weight, description, created_at, updated_at`;
    const result = await query<PetModel>(text, [
      id,
      owner_id,
      name,
      species || null,
      breed,
      size,
      age || null,
      weight || null,
      description || null,
    ]);

    if (result.rowCount === 0) throw new Error('Failed to create pet');

    const pet = result.rows[0];
    res.status(201).json(formatPet(pet));
  } catch (err: any) {
    console.error(err);
    if (err.code === '23503') {
      return res.status(400).json({ error: 'Invalid owner_id: owner not found' });
    }
    res.status(500).json({ error: 'Error creating pet' });
  }
}

/**
 * Create multiple pets for one owner
 * POST /api/pets/bulk
 */
export async function createPetsBulk(req: Request, res: Response) {
  const { owner_id, pets } = req.body as { owner_id?: string | number; pets?: any[] };
  if (owner_id == null || (typeof owner_id !== 'string' && typeof owner_id !== 'number') || String(owner_id).trim() === '') {
    return res.status(400).json({ error: 'owner_id is required' });
  }

  const petErrors = validatePetsArray(pets);
  if (petErrors.length > 0) {
    return res.status(400).json({ error: petErrors.join('; ') });
  }

  const petList = pets as any[];
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const insertedPets: PetModel[] = [];

    for (const pet of petList) {
      const id = uuidv4();
      const text = `INSERT INTO pets (id, owner_id, name, species, breed, size, age, weight, description, created_at, updated_at)
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, NOW(), NOW())
                    RETURNING id, owner_id, name, species, breed, size, age, weight, description, created_at, updated_at`;
      const result = await client.query<PetModel>(text, [
        id,
        owner_id,
        pet.name,
        pet.species || null,
        pet.breed,
        pet.size,
        pet.age || null,
        pet.weight || null,
        pet.description || null,
      ]);
      insertedPets.push(result.rows[0]);
    }

    await client.query('COMMIT');
    res.status(201).json(insertedPets.map((pet) => formatPet(pet)));
  } catch (err: any) {
    await client.query('ROLLBACK');
    console.error(err);
    if (err.code === '23503') {
      return res.status(400).json({ error: 'Invalid owner_id: owner not found' });
    }
    res.status(500).json({ error: 'Error creating pets' });
  } finally {
    client.release();
  }
}

/**
 * Update an existing pet
 * PUT /api/pets/:id
 */
export async function updatePet(req: Request, res: Response) {
  try {
    const { id } = req.params;
    const { name, species, breed, size, age, weight, description } = req.body as {
      name?: string | null;
      species?: string | null;
      breed?: string | null;
      size?: string | null;
      age?: number | null;
      weight?: number | null;
      description?: string | null;
    };

    if (!id) return res.status(400).json({ error: 'id is required' });

    const text = `UPDATE pets
                  SET name = COALESCE($1, name),
                      species = COALESCE($2, species),
                      breed = COALESCE($3, breed),
                      size = COALESCE($4, size),
                      age = COALESCE($5, age),
                      weight = COALESCE($6, weight),
                      description = COALESCE($7, description),
                      updated_at = NOW()
                  WHERE id = $8
                  RETURNING id, owner_id, name, species, breed, size, age, weight, description, created_at, updated_at`;
    const result = await query<PetModel>(text, [name, species, breed, size, age, weight, description, id]);

    if (result.rowCount === 0) return res.status(404).json({ error: 'Pet not found' });

    const pet = result.rows[0];
    res.json(formatPet(pet));
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error updating pet' });
  }
}

/**
 * Delete a pet
 * DELETE /api/pets/:id
 */
export async function deletePet(req: Request, res: Response) {
  try {
    const { id } = req.params;
    if (!id) return res.status(400).json({ error: 'id is required' });

    const text = 'DELETE FROM pets WHERE id = $1';
    const result = await query(text, [id]);

    if (result.rowCount === 0) return res.status(404).json({ error: 'Pet not found' });

    res.status(204).send();
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error deleting pet' });
  }
}

/**
 * Get all pets (admin only)
 * GET /api/pets/all
 */
export async function getAllPets(req: Request, res: Response) {
  try {
    const text = 'SELECT id, owner_id, name, species, breed, size, age, weight, description, created_at, updated_at FROM pets ORDER BY created_at DESC';
    const result = await query<PetModel>(text);
    const pets = result.rows.map((pet) => formatPet(pet));
    res.json(pets);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error fetching pets' });
  }
}

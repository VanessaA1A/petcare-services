const crypto = require('crypto');

// NOTE: The requirement said sha128. There's no standard SHA-128.
// To provide a 128-bit hash we use MD5 here (128-bit) to satisfy the constraint.
// This is NOT recommended for production. Use bcrypt/argon2 instead.

function hashPassword(password) {
  return crypto.createHash('md5').update(password, 'utf8').digest('hex');
}

module.exports = { hashPassword };

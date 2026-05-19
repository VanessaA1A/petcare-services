import crypto from 'crypto';

export function hashPassword(password: string): string {
  return crypto.createHash('md5').update(password, 'utf8').digest('hex');
}

import { verifyToken } from '../src/auth.js';

describe('auth', () => {
    test('rejects invalid token', () => {
        expect(verifyToken('invalid.token')).toBeNull();
    });

    test('rejects empty token', () => {
        expect(verifyToken('')).toBeNull();
    });
});
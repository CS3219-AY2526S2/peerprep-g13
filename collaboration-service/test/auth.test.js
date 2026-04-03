import { verifyToken } from '../src/auth.js';

describe('auth', () => {
    test('rejects invalid token', () => {
        expect(() => verifyToken('invalid.token', 'secret')).toThrow();
    });

    test('rejects empty token', () => {
        expect(() => verifyToken('', 'secret')).toThrow();
    });
});
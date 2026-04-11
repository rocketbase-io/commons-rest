import { createContext } from 'react';
import type { AuthContextProps } from './types';

/**
 * React context for authentication.
 * Provides axios client, base URL resolver, and token service.
 */
export const AuthContext = createContext<AuthContextProps | undefined>(undefined);

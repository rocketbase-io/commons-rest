import type { AxiosInstance } from 'axios';

/**
 * Service for managing authentication tokens.
 */
export interface TokenService {
  /**
   * Check if user is currently logged in
   */
  isLoggedIn: () => boolean;

  /**
   * Get the current authentication token.
   * Returns null if not logged in.
   */
  token: () => string | null;

  /**
   * Optional: Update/refresh the authentication token.
   * Should return the new token.
   */
  updateToken?: () => Promise<string>;
}

/**
 * Authentication context properties available via useAuth hook.
 */
export interface AuthContextProps {
  /**
   * Pre-configured axios instance with authentication interceptor
   */
  axiosClient: AxiosInstance;

  /**
   * Get base URL for a named client.
   * @param key - Optional client key. If not provided, returns default URL.
   */
  baseUrl: (key?: string) => string;

  /**
   * Token service instance
   */
  tokenService: TokenService;
}

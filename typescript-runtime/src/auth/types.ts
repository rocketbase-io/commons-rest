import type { AxiosInstance } from 'axios';

/**
 * Service for managing authentication tokens.
 *
 * Simple interface that works with most auth providers:
 * - Keycloak: () => keycloak.token ?? null
 * - Better-Auth: async () => (await getSession())?.accessToken ?? null
 * - Custom JWT: () => localStorage.getItem('token')
 */
export interface TokenService {
  /**
   * Get the current access token.
   * Returns null if not authenticated.
   *
   * Can be sync or async - the interceptor handles both.
   * The implementation should handle token refresh internally if needed.
   *
   * @returns Current access token or null
   */
  getToken: () => string | null | Promise<string | null>;

  /**
   * Optional callback invoked when a 401 Unauthorized response is received.
   * Use this to redirect to login, clear session, etc.
   *
   * @param error - The axios error object
   */
  onUnauthorized?: (error: unknown) => void | Promise<void>;
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

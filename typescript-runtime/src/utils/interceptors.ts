import type { InternalAxiosRequestConfig } from 'axios';
import type { TokenService } from '../auth/types';

/**
 * Axios request interceptor that adds Bearer token to all requests.
 * Automatically refreshes token if updateToken is available.
 *
 * @param tokenService - Token service instance
 * @returns Request interceptor function
 *
 * @example
 * ```typescript
 * const axiosInstance = axios.create();
 * axiosInstance.interceptors.request.use(bearerInterceptor(tokenService));
 * ```
 */
export function bearerInterceptor(tokenService: TokenService) {
  return async (config: InternalAxiosRequestConfig): Promise<InternalAxiosRequestConfig> => {
    if (!tokenService.isLoggedIn()) {
      return config;
    }

    // Update token if possible
    let token = tokenService.token();
    if (tokenService.updateToken) {
      try {
        token = await tokenService.updateToken();
      } catch (error) {
        // If token update fails, use existing token
        console.warn('Failed to update token:', error);
      }
    }

    // Add Bearer token to headers
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  };
}

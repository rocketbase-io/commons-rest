import type { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import type { TokenService } from '../auth/types';

/**
 * Axios request interceptor that adds Bearer token to all requests.
 *
 * Supports both sync and async token providers - compatible with:
 * - Keycloak (keycloak.token)
 * - Better-Auth (async session lookup)
 * - Custom JWT (localStorage, cookies, etc.)
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
    // Get token (handles both sync and async)
    const token = await Promise.resolve(tokenService.getToken());

    // Add Bearer token to headers if available
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  };
}

/**
 * Axios response interceptor that handles 401 Unauthorized responses.
 *
 * Invokes the optional onUnauthorized callback when a 401 is received.
 * Use this to redirect to login, clear session, show a notification, etc.
 *
 * @param tokenService - Token service instance
 * @returns Response interceptor tuple [onFulfilled, onRejected]
 *
 * @example
 * ```typescript
 * const axiosInstance = axios.create();
 * axiosInstance.interceptors.response.use(...unauthorizedInterceptor(tokenService));
 * ```
 */
export function unauthorizedInterceptor(tokenService: TokenService) {
  const onFulfilled = (response: AxiosResponse) => response;

  const onRejected = async (error: AxiosError) => {
    // Check if it's a 401 Unauthorized response
    if (error.response?.status === 401 && tokenService.onUnauthorized) {
      try {
        await Promise.resolve(tokenService.onUnauthorized(error));
      } catch (callbackError) {
        console.error('Error in onUnauthorized callback:', callbackError);
      }
    }

    // Always reject to allow error handling downstream
    return Promise.reject(error);
  };

  return [onFulfilled, onRejected] as const;
}

import { useMemo, type ReactNode } from 'react';
import axios, { type AxiosInstance } from 'axios';
import { AuthContext } from './AuthContext';
import type { TokenService } from './types';
import { bearerInterceptor } from '../utils/interceptors';

export interface AuthProviderProps {
  /**
   * React children to render
   */
  children: ReactNode;

  /**
   * Token service for authentication
   */
  tokenService: TokenService;

  /**
   * Base URL(s) for API clients.
   * - If string: Single base URL for all clients
   * - If Record: Named base URLs (e.g., { 'main': 'https://api.example.com', 'auth': 'https://auth.example.com' })
   */
  baseUrl: string | Record<string, string>;

  /**
   * Optional function to configure the axios instance.
   * Called after the instance is created but before interceptors are added.
   *
   * @param instance - The axios instance to configure
   *
   * @example
   * ```typescript
   * <AuthProvider
   *   axiosConfigure={(instance) => {
   *     instance.defaults.timeout = 5000;
   *     instance.defaults.headers.common['X-Custom-Header'] = 'value';
   *   }}
   * />
   * ```
   */
  axiosConfigure?: (instance: AxiosInstance) => void;
}

/**
 * Authentication provider component.
 * Provides authentication context to all child components.
 *
 * @example Single client
 * ```tsx
 * <AuthProvider
 *   tokenService={myTokenService}
 *   baseUrl="https://api.example.com"
 * >
 *   <App />
 * </AuthProvider>
 * ```
 *
 * @example Multiple named clients
 * ```tsx
 * <AuthProvider
 *   tokenService={myTokenService}
 *   baseUrl={{
 *     'main': 'https://api.example.com',
 *     'auth': 'https://auth.example.com',
 *   }}
 * >
 *   <App />
 * </AuthProvider>
 * ```
 */
export function AuthProvider({
  children,
  tokenService,
  baseUrl,
  axiosConfigure,
}: AuthProviderProps) {
  const contextValue = useMemo(() => {
    // Create axios instance
    const axiosClient = axios.create({
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Apply custom configuration if provided
    if (axiosConfigure) {
      axiosConfigure(axiosClient);
    }

    // Add bearer token interceptor
    axiosClient.interceptors.request.use(bearerInterceptor(tokenService));

    // Create base URL resolver
    const baseUrlFn = (key?: string): string => {
      if (typeof baseUrl === 'string') {
        return baseUrl;
      }

      // If key provided, try to get that URL
      if (key && baseUrl[key]) {
        return baseUrl[key];
      }

      // Fallback to 'default' key
      if (baseUrl['default']) {
        return baseUrl['default'];
      }

      // If no default, return first URL
      const firstKey = Object.keys(baseUrl)[0];
      return firstKey ? baseUrl[firstKey] : '';
    };

    return {
      axiosClient,
      baseUrl: baseUrlFn,
      tokenService,
    };
  }, [tokenService, baseUrl, axiosConfigure]);

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
}

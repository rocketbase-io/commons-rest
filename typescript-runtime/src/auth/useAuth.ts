import { useContext } from 'react';
import { AuthContext } from './AuthContext';
import type { AuthContextProps } from './types';

/**
 * Hook to access authentication context.
 * Must be used within an AuthProvider.
 *
 * @returns Authentication context with axios client, base URL resolver, and token service
 * @throws Error if used outside of AuthProvider
 *
 * @example
 * ```tsx
 * function MyComponent() {
 *   const { axiosClient, baseUrl, tokenService } = useAuth();
 *   const api = createMyApi(axiosClient, { baseURL: baseUrl() });
 *   // ...
 * }
 * ```
 */
export function useAuth(): AuthContextProps {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return context;
}

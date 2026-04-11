# Auth Provider Integration Examples

## Keycloak Integration

```typescript
import Keycloak from 'keycloak-js';
import { AuthProvider, type TokenService } from '@rocketbase/commons-rest-client';

const keycloak = new Keycloak({
  url: 'https://auth.example.com',
  realm: 'my-realm',
  clientId: 'my-app',
});

await keycloak.init({ onLoad: 'login-required' });

const tokenService: TokenService = {
  getToken: () => keycloak.token ?? null,
  onUnauthorized: () => keycloak.login(),
};

function App() {
  return (
    <AuthProvider
      tokenService={tokenService}
      baseUrl="https://api.example.com"
    >
      {/* Your app */}
    </AuthProvider>
  );
}
```

## Better-Auth Integration

```typescript
import { createAuthClient } from 'better-auth/react';
import { AuthProvider, type TokenService } from '@rocketbase/commons-rest-client';

const authClient = createAuthClient({
  baseURL: 'https://api.example.com',
});

const tokenService: TokenService = {
  getToken: async () => {
    const session = await authClient.getSession();
    return session.data?.session.token ?? null;
  },
  onUnauthorized: () => {
    window.location.href = '/login';
  },
};

function App() {
  return (
    <AuthProvider
      tokenService={tokenService}
      baseUrl="https://api.example.com"
    >
      {/* Your app */}
    </AuthProvider>
  );
}
```

## Custom JWT (localStorage)

```typescript
import { AuthProvider, type TokenService } from '@rocketbase/commons-rest-client';

const tokenService: TokenService = {
  getToken: () => localStorage.getItem('access_token'),
  onUnauthorized: () => {
    localStorage.removeItem('access_token');
    window.location.href = '/login';
  },
};

function App() {
  return (
    <AuthProvider
      tokenService={tokenService}
      baseUrl="https://api.example.com"
    >
      {/* Your app */}
    </AuthProvider>
  );
}
```

## Multi-Client Setup with Named URLs

```typescript
import { AuthProvider, type TokenService } from '@rocketbase/commons-rest-client';

const tokenService: TokenService = {
  getToken: () => localStorage.getItem('access_token'),
  onUnauthorized: async () => {
    // Try to refresh token
    try {
      const response = await fetch('https://auth.example.com/refresh', {
        method: 'POST',
        credentials: 'include',
      });
      const { token } = await response.json();
      localStorage.setItem('access_token', token);
    } catch {
      // Redirect to login if refresh fails
      window.location.href = '/login';
    }
  },
};

function App() {
  return (
    <AuthProvider
      tokenService={tokenService}
      baseUrl={{
        main: 'https://api.example.com',
        auth: 'https://auth.example.com',
        analytics: 'https://analytics.example.com',
      }}
    >
      {/* Your app */}
    </AuthProvider>
  );
}

// Usage in generated client:
const api = useApi();
api.userApi.getProfile(); // Uses 'main' URL
api.authApi.login(); // Uses 'auth' URL
```

## Custom Axios Configuration

```typescript
import { AuthProvider, type TokenService } from '@rocketbase/commons-rest-client';

const tokenService: TokenService = {
  getToken: () => localStorage.getItem('access_token'),
};

function App() {
  return (
    <AuthProvider
      tokenService={tokenService}
      baseUrl="https://api.example.com"
      axiosConfigure={(instance) => {
        // Add custom timeout
        instance.defaults.timeout = 10000;

        // Add custom headers
        instance.defaults.headers.common['X-App-Version'] = '1.0.0';

        // Add request interceptor for logging
        instance.interceptors.request.use((config) => {
          console.log('Request:', config.method, config.url);
          return config;
        });

        // Add response interceptor for error handling
        instance.interceptors.response.use(
          (response) => response,
          (error) => {
            console.error('API Error:', error);
            return Promise.reject(error);
          }
        );
      }}
    >
      {/* Your app */}
    </AuthProvider>
  );
}
```

## Auth.js (NextAuth) Integration

```typescript
import { useSession } from 'next-auth/react';
import { AuthProvider, type TokenService } from '@rocketbase/commons-rest-client';

function AuthWrapper({ children }: { children: React.ReactNode }) {
  const { data: session } = useSession();

  const tokenService: TokenService = {
    getToken: () => session?.accessToken as string ?? null,
    onUnauthorized: () => {
      // NextAuth handles this automatically
      window.location.href = '/api/auth/signin';
    },
  };

  return (
    <AuthProvider
      tokenService={tokenService}
      baseUrl={process.env.NEXT_PUBLIC_API_URL!}
    >
      {children}
    </AuthProvider>
  );
}
```

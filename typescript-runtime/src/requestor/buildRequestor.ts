import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios';
import type { RequestorBuildConfig, RequestorBuilder } from './types';
import { applyIfNecessary, buildFromKeys, mergeRequestConfig } from './utils';

/**
 * Default axios request configuration.
 * Sets Content-Type to application/json and configures params serializer.
 */
export function buildDefaultAxiosRequestConfig(): AxiosRequestConfig {
  return {
    headers: {
      'Content-Type': 'application/json',
    },
    paramsSerializer: {
      indexes: null, // Don't add brackets to array params
    },
  };
}

/**
 * Build a requestor function from configuration.
 * The returned function will execute HTTP requests based on the provided config.
 *
 * Automatically handles AbortSignal from React Query for request cancellation.
 *
 * @template Options - Request options type
 * @template Result - Expected response type
 * @param config - Requestor configuration
 * @param defaults - Default axios config to merge
 * @returns Requestor function that executes the HTTP request
 *
 * @example Basic usage
 * ```typescript
 * const getUser = buildRequestor<{ id: string }, User>({
 *   method: 'get',
 *   url: ({ id }) => `/users/${id}`,
 * });
 *
 * const user = await getUser({ id: '123' });
 * ```
 *
 * @example React Query integration with automatic cancellation
 * ```typescript
 * const getUsers = buildRequestor<{ page: number }, PageableResult<User>>({
 *   method: 'get',
 *   url: '/users',
 *   params: ['page'],
 * });
 *
 * // React Query automatically passes signal for cancellation
 * const query = useQuery({
 *   queryKey: ['users', page],
 *   queryFn: ({ signal }) => getUsers({ page, signal }),
 * });
 * ```
 */
export function buildRequestor<Options extends Record<string, unknown>, Result>(
  config: RequestorBuildConfig<Options, Result>,
  defaults?: AxiosRequestConfig
): RequestorBuilder<Options, Result> {
  const client = config.client ?? axios.create();

  return async (options: Options): Promise<Result> => {
    // Build request config from options
    const method = applyIfNecessary(config.method, options) ?? 'get';
    const url = applyIfNecessary(config.url, options);

    // Build headers
    let headers: unknown;
    if (Array.isArray(config.headers)) {
      headers = buildFromKeys(config.headers, options);
    } else if (config.headers) {
      headers = applyIfNecessary(config.headers, options);
    }

    // Build params
    let params: unknown;
    if (Array.isArray(config.params)) {
      params = buildFromKeys(config.params, options);
    } else if (config.params) {
      params = applyIfNecessary(config.params, options);
    }

    // Build body
    let data: unknown;
    if (Array.isArray(config.body)) {
      data = buildFromKeys(config.body, options);
    } else if (config.body) {
      data = applyIfNecessary(config.body, options);
    }

    // Build additional options
    const additionalOptions = applyIfNecessary(config.options, options);

    // Extract AbortSignal if present in options (for React Query integration)
    // React Query passes { signal?: AbortSignal } to queryFn
    const signal =
      'signal' in options && options.signal instanceof AbortSignal
        ? options.signal
        : undefined;

    // Merge all configs
    const requestConfig = mergeRequestConfig(
      buildDefaultAxiosRequestConfig(),
      defaults,
      {
        method,
        url,
        headers: headers as Record<string, string>,
        params: params as Record<string, unknown>,
        data,
        signal, // Add signal for request cancellation
      },
      additionalOptions
    );

    // Execute request
    const response = await client.request<Result>(requestConfig);
    return response.data;
  };
}

/**
 * Create a requestor factory with pre-configured client and defaults.
 * The factory can be used to build multiple requestor functions with shared configuration.
 *
 * @param client - Optional axios instance
 * @param defaults - Default axios configs to merge (can be multiple)
 * @returns Function to build requestor functions
 *
 * @example
 * ```typescript
 * const builder = buildRequestorFactory(axiosClient, { baseURL: 'https://api.example.com' });
 *
 * const getUser = builder<{ id: string }, User>({
 *   method: 'get',
 *   url: ({ id }) => `/users/${id}`,
 * });
 *
 * const createUser = builder<{ body: UserInput }, User>({
 *   method: 'post',
 *   url: '/users',
 *   body: ({ body }) => body,
 * });
 * ```
 */
export function buildRequestorFactory(
  client?: AxiosInstance,
  ...defaults: (AxiosRequestConfig | undefined)[]
) {
  const mergedDefaults = mergeRequestConfig(...defaults);

  return <Options extends Record<string, unknown>, Result>(
    config: Omit<RequestorBuildConfig<Options, Result>, 'client'>
  ): RequestorBuilder<Options, Result> => {
    return buildRequestor<Options, Result>(
      {
        ...config,
        client,
      },
      mergedDefaults
    );
  };
}

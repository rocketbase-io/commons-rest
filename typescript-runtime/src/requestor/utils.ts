import type { AxiosRequestConfig } from 'axios';

/**
 * Deep merge axios request configurations.
 * Handles special cases for headers, params, and other nested objects.
 *
 * @param configs - Array of axios configs to merge
 * @returns Merged configuration
 */
export function mergeRequestConfig(...configs: (AxiosRequestConfig | undefined)[]): AxiosRequestConfig {
  const result: AxiosRequestConfig = {};

  for (const config of configs) {
    if (!config) continue;

    // Merge simple properties
    Object.assign(result, config);

    // Deep merge headers
    if (config.headers) {
      result.headers = {
        ...result.headers,
        ...config.headers,
      };
    }

    // Deep merge params
    if (config.params) {
      result.params = {
        ...result.params,
        ...config.params,
      };
    }

    // Deep merge auth
    if (config.auth) {
      result.auth = {
        ...result.auth,
        ...config.auth,
      };
    }

    // Deep merge proxy
    if (config.proxy) {
      result.proxy = {
        ...result.proxy,
        ...config.proxy,
      };
    }
  }

  return result;
}

/**
 * Apply a value that can be either static or a function.
 * If the value is a function, call it with the provided options.
 *
 * @param value - Static value or function
 * @param options - Options to pass to function if value is a function
 * @returns Resolved value
 */
export function applyIfNecessary<Options, Result>(
  value: Result | ((options: Options) => Result) | undefined,
  options: Options
): Result | undefined {
  if (value === undefined) {
    return undefined;
  }

  if (typeof value === 'function') {
    return (value as (options: Options) => Result)(options);
  }

  return value;
}

/**
 * Build an object from option keys.
 * Extracts specified keys from options object.
 *
 * @param keys - Array of keys to extract
 * @param options - Source options object
 * @returns Object with extracted keys
 */
export function buildFromKeys<Options extends object>(
  keys: (keyof Options)[],
  options: Options
): Record<string, unknown> {
  const result: Record<string, unknown> = {};
  // Cast once at the boundary: TypeScript's `extends object` bound matches any
  // interface / record / class instance, but doesn't permit `obj[key]` indexing
  // by itself. The cast is internal — callers stay strictly typed via `Options`.
  const indexable = options as Record<string, unknown>;

  for (const key of keys) {
    const strKey = String(key);
    if (strKey in indexable) {
      result[strKey] = indexable[strKey];
    }
  }

  return result;
}

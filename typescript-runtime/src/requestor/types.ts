import type { AxiosInstance, AxiosRequestConfig, Method } from 'axios';

/**
 * Function that builds and executes an HTTP request.
 * @template Options - Request options type
 * @template Result - Expected response type
 */
export type RequestorBuilder<Options, Result> = (options: Options) => Promise<Result>;

/**
 * Configuration for building a requestor function.
 * @template Options - Request options type
 * @template Result - Expected response type
 * @template Error - Expected error type
 */
export interface RequestorBuildConfig<Options, Result, Error = unknown> {
  /**
   * Optional axios instance to use.
   * If not provided, a default instance will be used.
   */
  client?: AxiosInstance;

  /**
   * HTTP method (GET, POST, PUT, DELETE, etc.).
   * Can be a static value or a function that derives it from options.
   */
  method?: Method | ((options: Options) => Method);

  /**
   * Request URL.
   * Can be a static string or a function that builds it from options.
   */
  url: string | ((options: Options) => string);

  /**
   * Request headers.
   * Can be an array of option keys to use as headers, or a function that builds headers from options.
   */
  headers?: (keyof Options)[] | ((options: Options) => unknown);

  /**
   * Query parameters.
   * Can be an array of option keys to use as params, or a function that builds params from options.
   */
  params?: (keyof Options)[] | ((options: Options) => unknown);

  /**
   * Request body.
   * Can be an array of option keys to use as body, or a function that builds body from options.
   */
  body?: (keyof Options)[] | ((options: Options) => unknown);

  /**
   * Additional axios request config.
   * Can be a static config or a function that derives it from options.
   */
  options?: AxiosRequestConfig | ((options: Options) => AxiosRequestConfig);
}

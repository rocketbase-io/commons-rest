import axios, { AxiosInstance,  AxiosRequestConfig, Method } from 'axios';
import qs from 'qs';

export interface RequestorBuildConfig<Options, Result, Error = unknown> {
  client?: AxiosInstance;
  method?: Method | ((options: Options) => Method);
  url: string | ((options: Options) => string);
  headers?: (keyof Options)[] | ((options: Options) => unknown);
  params?: (keyof Options)[] | ((options: Options) => unknown);
  body?: (keyof Options)[] | ((options: Options) => unknown);
  options?: AxiosRequestConfig | ((options: Options) => AxiosRequestConfig);
}

const deep = ['headers', 'auth', 'proxy', 'params'];

function mergeRequestConfig(
  ...configs: (AxiosRequestConfig | undefined)[]
): AxiosRequestConfig {
  const result: AxiosRequestConfig = {};
  for (const config of configs.map((it = {}) => it)) {
    for (const key of Object.keys(config) as (keyof typeof config)[]) {
      if (config[key] == null) continue;
      else if (!deep.includes(key)) {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        result[key] = config[key];
      } else {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        result[key] = {...(result[key] ?? {}), ...(config[key] ?? {})};
      }
    }
  }

  return result;
}

export type RequestorBuilder<Options, Result> = (
  options: Options & { overrides?: AxiosRequestConfig }
) => Promise<Result>;

function applyIfNecessary<Options, Result>(
  element: ((options: Options) => Result) | Result,
  options: Options
): Result {
  if (typeof element === 'function')
    return (element as (options: Options) => Result)(options);
  return element;
}

export function buildRequestor<Options, Result, Error = unknown>(
  config: RequestorBuildConfig<Options, Result, Error>,
  cf?: AxiosRequestConfig
): RequestorBuilder<Options, Result> {
  const {
    client = axios,
    method = 'get',
    url,
    headers,
    params,
    body,
    options: clientOptions
  } = config;
  return function ({overrides, ...options}) {
    const config = mergeRequestConfig(
      cf,
      {
        method: applyIfNecessary(method, options as Options),
        url: applyIfNecessary(url, options as Options),
        headers: applyIfNecessary(headers, options as Options) as never,
        params: applyIfNecessary(params, options as Options),
        data: applyIfNecessary(body, options as Options)
      },
      applyIfNecessary(clientOptions, options as Options),
      overrides
    );
    return client.request(config).then(resp => resp.data);
  };
}

export function buildDefaultAxiosRequestConfig(...configs: (AxiosRequestConfig | undefined)[]) {
  return mergeRequestConfig(
    {
      headers: {'content-type': 'application/json'},
      paramsSerializer: (params) =>
        qs.stringify(params, {arrayFormat: 'repeat'})
    },
    ...configs
  );
}

export function buildRequestorFactory(
  client: AxiosInstance = axios,
  ...configs: (AxiosRequestConfig | undefined)[]
): typeof buildRequestor {
  return (config) => buildRequestor({client, ...config},
    buildDefaultAxiosRequestConfig(...configs)
  );
}

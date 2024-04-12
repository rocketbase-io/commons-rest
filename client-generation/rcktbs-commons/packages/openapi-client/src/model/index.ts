export * from './openapi';

export interface PageableRequest {
  page?: unknown;
  pageSize?: number;
  sort?: string[];
}

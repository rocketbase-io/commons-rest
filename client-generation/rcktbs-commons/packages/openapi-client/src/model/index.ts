export * from './openapi';

export interface PageableRequest {
  page?: number;
  pageSize?: number;
  sort?: string[];
}

import type { PageableResult } from '../models';

/**
 * Type definition for InfiniteData from @tanstack/react-query.
 * This is defined here to avoid direct dependency on react-query.
 */
export interface InfiniteData<TData> {
  pages: TData[];
  pageParams: unknown[];
}

/**
 * Creates pagination options for React Query infinite queries.
 * Works with both react-query v4 and v5.
 *
 * @template T - PageableResult type
 * @returns Pagination options object with getPreviousPageParam, getNextPageParam, and initialPageParam
 *
 * @example
 * ```typescript
 * const infiniteQuery = useInfiniteQuery({
 *   queryKey: ['users'],
 *   queryFn: ({ pageParam = 0 }) => fetchUsers({ page: pageParam }),
 *   ...createPaginationOptions(),
 * });
 * ```
 */
export function createPaginationOptions<T extends PageableResult<unknown>>() {
  return {
    /**
     * Get the previous page parameter.
     * Returns the previous page number if not on first page, otherwise undefined.
     */
    getPreviousPageParam: ({ page }: T) => {
      return page > 0 ? page - 1 : undefined;
    },

    /**
     * Get the next page parameter.
     * Returns the next page number if not on last page, otherwise undefined.
     */
    getNextPageParam: ({ page, totalPages }: T) => {
      return page < totalPages - 1 ? page + 1 : undefined;
    },

    /**
     * Initial page parameter (required for react-query v5)
     */
    initialPageParam: 0,
  };
}

/**
 * Extracts total element count from infinite query data.
 * Returns the totalElements from the first page, or 0 if no data.
 *
 * @template T - Content type
 * @param data - Infinite query data
 * @returns Total number of elements across all pages
 *
 * @example
 * ```typescript
 * const { data } = useInfiniteQuery(...);
 * const total = infiniteTotalElements(data);
 * console.log(`Showing ${data?.pages.length ?? 0} pages of ${total} items`);
 * ```
 */
export function infiniteTotalElements<T>(
  data: InfiniteData<PageableResult<T>> | undefined
): number {
  if (!data || !Array.isArray(data.pages) || data.pages.length === 0) {
    return 0;
  }

  return data.pages[0].totalElements;
}

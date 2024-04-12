import { PageableResult } from "@rocketbase/commons-core";
import { InfiniteData } from '@tanstack/react-query';

export function createPaginationOptions<T extends PageableResult<unknown>>() {
  return {
    getPreviousPageParam: ({page } : T) => {
      return page !== 0 ? page - 1 : 0;
    },
    getNextPageParam: ({page, totalPages}:  T) => {
      return page < totalPages - 1 ? page + 1 : null;
    },
    initialPageParam: 0
  }
}


export function infiniteTotalElements<T>(
  data: InfiniteData<PageableResult<T>>
): number {
  if (data && Array.isArray(data?.pages) && data?.pages.length) {
    return data.pages[0].totalElements;
  }
  return 0;
}


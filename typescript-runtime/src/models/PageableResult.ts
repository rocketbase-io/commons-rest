/**
 * Standard pageable result wrapper for REST APIs.
 * Matches Spring Data's Page interface structure.
 */
export interface PageableResult<T> {
  /**
   * Array of items for the current page
   */
  content: T[];

  /**
   * Total number of elements across all pages
   */
  totalElements: number;

  /**
   * Total number of pages
   */
  totalPages: number;

  /**
   * Current page number (0-indexed)
   */
  page: number;

  /**
   * Maximum number of items per page
   */
  pageSize: number;
}

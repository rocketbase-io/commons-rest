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

/**
 * Extended pageable result with additional metadata support.
 * <p>
 * This implementation allows you to attach custom metadata to paginated results.
 * The metadata field can be used for various purposes such as:
 * <ul>
 *   <li>Summary information (totals, aggregations, statistics)</li>
 *   <li>GeoJSON feature collections</li>
 *   <li>Revision hashes or versioning information</li>
 *   <li>Any other domain-specific metadata</li>
 * </ul>
 * </p>
 *
 * @template T - the type of elements in the page content
 * @template M - the type of metadata attached to this result
 */
export interface PageableResultWithMeta<T, M> extends PageableResult<T> {
  /**
   * Additional metadata (summary, GeoJSON, etc.)
   */
  meta?: M;
}

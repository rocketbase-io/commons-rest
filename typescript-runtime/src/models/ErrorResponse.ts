/**
 * Standard error response from REST APIs.
 */
export interface ErrorResponse {
  /**
   * HTTP status code
   */
  status: number;

  /**
   * User-readable error message
   */
  message: string;

  /**
   * Optional field-level validation errors.
   * Maps field names to arrays of error messages.
   */
  fields?: Record<string, string[]>;
}

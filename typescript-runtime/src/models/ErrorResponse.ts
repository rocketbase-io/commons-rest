/**
 * Standard error response from REST APIs.
 * Follows RFC 9457 (problem details for HTTP APIs).
 */
export interface ErrorResponse {
  /**
   * URI reference that identifies the problem type.
   * Defaults to "about:blank" when not set.
   */
  type?: string;

  /**
   * Short, human-readable summary of the problem type.
   */
  title?: string;

  /**
   * HTTP status code
   */
  status: number;

  /**
   * Human-readable explanation specific to this occurrence of the problem.
   */
  detail?: string;

  /**
   * URI reference that identifies the specific occurrence of the problem.
   */
  instance?: string;

  /**
   * Optional field-level validation errors.
   * Maps field names to arrays of error messages.
   * Extension member as allowed by RFC 9457.
   */
  fields?: Record<string, string[]>;
}

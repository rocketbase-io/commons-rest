/**
 * wrapped response in case of errors
 */
export interface ErrorResponse {
    status: number;
    message: string;
    fields?: Record<string, string[]>;
}

/**
 * wrapping object for paged result lists
 */
export interface PageableResult<E> {
    totalElements: number;
    totalPages: number;
    page: number;
    pageSize: number;
    content: E[];
}

/**
 * simple address object
 */
export interface AddressDto {
    addressLineOne?: string;
    addressLineTwo?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    countryCode?: string;
}

export interface ContactDto extends HasFirstAndLastName {
    gender?: Gender;
    salutation?: string;
    title?: string;
    email?: string;
    landline?: string;
    cellphone?: string;
}

/**
 * Object representation of validation constraints defined on a model domain object
 *
 * <b>Sample JSON serialization:</b>
 * <pre>
 * {
 *   "model": "io.rocketbase.commons.model.User",
 *      "constraints": {
 *          "lastName": [{
 *              "type": "NotBlank",
 *              "message": "may not be empty"
 *          }],
 *          "email": [{
 *              "type": "NotNull",
 *              "message": "may not be null"
 *           }, {
 *              "type": "Email",
 *              "message": "not a well-formed email address",
 *              "flags": [],
 *              "regexp": ".*"
 *          }],
 *          "login": [{
 *              "type": "NotNull",
 *              "message": "may not be null"
 *          }, {
 *              "type": "Length",
 *              "message": "length must be between 8 and 2147483647",
 *              "min": 8,
 *              "max": 2147483647
 *          }],
 *          "firstName": [{
 *              "type": "NotBlank",
 *              "message": "may not be empty"
 *          }]
 *      }
 * }
 * </pre>
 * @see ValidationConstraint
 */
export interface ModelConstraint {
    model: string;
    constraints: Record<string, string>;
}

/**
 * Object representation of validation constraints.
 *
 * <b>Sample JSON serialization:</b>
 * <pre>
 *          "lastName": [{
 *              "type": "NotBlank",
 *              "message": "may not be empty"
 *          }],
 *          "email": [{
 *              "type": "NotNull",
 *              "message": "may not be null"
 *           }, {
 *              "type": "Email",
 *              "message": "not a well-formed email address",
 *              "flags": [],
 *              "regexp": ".*"
 *          }]
 * </pre>
 */
export interface ValidationConstraint {
    type: string;
    message: string;
    attributes: Record<string, string>;
}

export interface EntityWithKeyValue<T> extends HasKeyValue {
}

/**
 * entity/dto has firstName + lastName capability
 */
export interface HasFirstAndLastName {
    firstName?: string;
    lastName?: string;
}

/**
 * entity/dto has key value capability
 */
export interface HasKeyValue {
    keyValues?: Record<string, string>;
}

export type Gender = "female" | "male" | "diverse";

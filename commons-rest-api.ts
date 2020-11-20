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
    title?: string;
    email?: string;
    landline?: string;
    cellphone?: string;
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

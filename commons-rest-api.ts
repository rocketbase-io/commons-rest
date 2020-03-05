export interface ErrorResponse {
    status: number;
    message: string;
    fields?: Record<string, string[]>;
}

export interface PageableResult<E> {
    totalElements: number;
    totalPages: number;
    page: number;
    pageSize: number;
    content: E[];
}

export interface AddressDto {
    addressLineOne?: string;
    addressLineTwo?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    countryCode?: string;
}

export interface ContactDto {
    gender?: Gender;
    title?: string;
    firstName?: string;
    lastName?: string;
    email?: string;
    landline?: string;
    cellphone?: string;
}

export type Gender = "female" | "male" | "diverse";

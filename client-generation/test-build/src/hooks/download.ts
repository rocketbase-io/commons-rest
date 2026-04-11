/* eslint-disable no-use-before-define */
// generated 2026-04-11 21:39:05
import type { OpenApiModuleApi } from "../../clients";import { useInfiniteQuery, useQuery, useMutation, useQueryClient, type UseQueryOptions, type UseInfiniteQueryOptions, type UseMutationOptions , type InfiniteData, queryOptions, infiniteQueryOptions  } from "@tanstack/react-query";
import type { PageableResult } from "@rocketbase/commons-rest-client";
import { useApi } from "../../clients";
import type { PageableRequest } from "../../model";
import { createPaginationOptions } from "../util";
import type { java.io.OutputStream, org.springframework.core.io.Resource } from "../../model";



/**
  * [GET] /download/ppt
  */
  export const queryOptionDownloadDownloadPpt= <TData = java.io.OutputStream, TError = Error>(openApiModuleApi:OpenApiModuleApi,  options: Omit<UseQueryOptions<java.io.OutputStream, TError, TData>, "queryFn" | "queryKey"> = {}) => queryOptions(
{
      queryKey: [`download`, `ppt`],
      queryFn: ({ signal }) => openApiModuleApi.download.downloadPpt({  overrides: { signal }}),
      ...options
});

export function useQueryDownloadDownloadPpt<TData = java.io.OutputStream, TError = Error>( options: Omit<UseQueryOptions<java.io.OutputStream, TError, TData>, "queryFn" | "queryKey"> = {}) {
    const { openApiModuleApi } = useApi();
    return useQuery<java.io.OutputStream, TError, TData>(queryOptionDownloadDownloadPpt(openApiModuleApi, options));
}
  
/**
  * [GET] /download/pdf
  */
  export const queryOptionDownloadDownloadPdf= <TData = org.springframework.core.io.Resource, TError = Error>(openApiModuleApi:OpenApiModuleApi,  options: Omit<UseQueryOptions<org.springframework.core.io.Resource, TError, TData>, "queryFn" | "queryKey"> = {}) => queryOptions(
{
      queryKey: [`download`, `pdf`],
      queryFn: ({ signal }) => openApiModuleApi.download.downloadPdf({  overrides: { signal }}),
      ...options
});

export function useQueryDownloadDownloadPdf<TData = org.springframework.core.io.Resource, TError = Error>( options: Omit<UseQueryOptions<org.springframework.core.io.Resource, TError, TData>, "queryFn" | "queryKey"> = {}) {
    const { openApiModuleApi } = useApi();
    return useQuery<org.springframework.core.io.Resource, TError, TData>(queryOptionDownloadDownloadPdf(openApiModuleApi, options));
}
  
/**
  * [GET] /download/excel
  */
  export const queryOptionDownloadDownloadExcel= <TData = number[], TError = Error>(openApiModuleApi:OpenApiModuleApi,  options: Omit<UseQueryOptions<number[], TError, TData>, "queryFn" | "queryKey"> = {}) => queryOptions(
{
      queryKey: [`download`, `excel`],
      queryFn: ({ signal }) => openApiModuleApi.download.downloadExcel({  overrides: { signal }}),
      ...options
});

export function useQueryDownloadDownloadExcel<TData = number[], TError = Error>( options: Omit<UseQueryOptions<number[], TError, TData>, "queryFn" | "queryKey"> = {}) {
    const { openApiModuleApi } = useApi();
    return useQuery<number[], TError, TData>(queryOptionDownloadDownloadExcel(openApiModuleApi, options));
}
  
/* eslint-disable no-use-before-define */
// generated 2026-04-11 21:39:05
import type { ActivityFindAll, ActivityFindById } from "../../clients";
import type { OpenApiModuleApi } from "../../clients";import { useInfiniteQuery, useQuery, useMutation, useQueryClient, type UseQueryOptions, type UseInfiniteQueryOptions, type UseMutationOptions , type InfiniteData, queryOptions, infiniteQueryOptions  } from "@tanstack/react-query";
import type { PageableResult } from "@rocketbase/commons-rest-client";
import { useApi } from "../../clients";
import type { PageableRequest } from "../../model";
import { createPaginationOptions } from "../util";
import type { io.rocketbase.commons.dto.PageableResult, io.rocketbase.commons.openapi.sample.dto.Activity } from "../../model";



/**
  * [GET] /activity
  * list all tiles (pinboards, searchconfigs, briefings or showrooms a user has access to
  */
  export const queryOptionActivityFindAll= <TData=io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>, TError = Error>(openApiModuleApi:OpenApiModuleApi, filter: ActivityFindAll, options: Omit<UseInfiniteQueryOptions<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>, TError, TData>, "queryFn" | "queryKey"  | "getPreviousPageParam" | "getNextPageParam" | "initialPageParam"> = {}) => infiniteQueryOptions(
{
    queryKey: [`activity`, `list`, filter ],
    queryFn: ({ pageParam, signal }) => {
        return openApiModuleApi.activity.findAll({ ...filter, page: pageParam, overrides: { signal } })
    },
    staleTime: 2 * 1000,     ...options,
    ...createPaginationOptions()});

export function useInfiniteActivityFindAll<TData = InfiniteData<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>>, TError = Error>(filter: ActivityFindAll, options: Omit<UseInfiniteQueryOptions<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>, TError, TData>, "queryFn" | "queryKey"  | "getPreviousPageParam" | "getNextPageParam" | "initialPageParam"> = {}) {
    const { openApiModuleApi } = useApi();
    return useInfiniteQuery<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>, TError, TData>(queryOptionActivityFindAll(openApiModuleApi, filter, options));
}

/**
  * [GET] /activity/{id}
  */
  export const queryOptionActivityFindById= <TData = io.rocketbase.commons.openapi.sample.dto.Activity, TError = Error>(openApiModuleApi:OpenApiModuleApi, filter: ActivityFindById, options: Omit<UseQueryOptions<io.rocketbase.commons.openapi.sample.dto.Activity, TError, TData>, "queryFn" | "queryKey"> = {}) => queryOptions(
{
      queryKey: [`activity`, `detail`, `${filter.id}`, filter ],
      queryFn: ({ signal }) => openApiModuleApi.activity.findById({ ...filter, overrides: { signal }}),
      staleTime: 2 * 1000, ...options
});

export function useQueryActivityFindById<TData = io.rocketbase.commons.openapi.sample.dto.Activity, TError = Error>(filter: ActivityFindById, options: Omit<UseQueryOptions<io.rocketbase.commons.openapi.sample.dto.Activity, TError, TData>, "queryFn" | "queryKey"> = {}) {
    const { openApiModuleApi } = useApi();
    return useQuery<io.rocketbase.commons.openapi.sample.dto.Activity, TError, TData>(queryOptionActivityFindById(openApiModuleApi,filter, options));
}
  
/**
  * [GET] /activity/validate/logs
  */
  export const queryOptionActivityGetAll= <TData=io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>, TError = Error>(openApiModuleApi:OpenApiModuleApi, filter: PageableRequest, options: Omit<UseInfiniteQueryOptions<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>, TError, TData>, "queryFn" | "queryKey"  | "getPreviousPageParam" | "getNextPageParam" | "initialPageParam"> = {}) => infiniteQueryOptions(
{
    queryKey: [`validate`, `logs`],
    queryFn: ({ pageParam, signal }) => {
        return openApiModuleApi.activity.getAll({ ...filter, page: pageParam, overrides: { signal } })
    },
    staleTime: 1 * 1000,     ...options,
    ...createPaginationOptions()});

export function useInfiniteActivityGetAll<TData = InfiniteData<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>>, TError = Error>( options: Omit<UseInfiniteQueryOptions<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>, TError, TData>, "queryFn" | "queryKey"  | "getPreviousPageParam" | "getNextPageParam" | "initialPageParam"> = {}) {
    const { openApiModuleApi } = useApi();
    return useInfiniteQuery<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>, TError, TData>(queryOptionActivityGetAll(openApiModuleApi,  options));
}

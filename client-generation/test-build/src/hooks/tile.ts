/* eslint-disable no-use-before-define */
// generated 2026-04-11 21:39:05
import type { TileFindOne, TileFindAll } from "../../clients";
import type { OpenApiModuleApi } from "../../clients";import { useInfiniteQuery, useQuery, useMutation, useQueryClient, type UseQueryOptions, type UseInfiniteQueryOptions, type UseMutationOptions , type InfiniteData, queryOptions, infiniteQueryOptions  } from "@tanstack/react-query";
import type { PageableResult } from "@rocketbase/commons-rest-client";
import { useApi } from "../../clients";
import type { PageableRequest } from "../../model";
import { createPaginationOptions } from "../util";
import type { io.rocketbase.commons.dto.PageableResult, io.rocketbase.commons.openapi.sample.dto.Tile, UserPreference } from "../../model";



/**
  * [GET] /tile
  * list all tiles (pinboards, searchconfigs, briefings or showrooms a user has access to
  */
  export const queryOptionTileFindAll= <TData=io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Tile>, TError = Error>(openApiModuleApi:OpenApiModuleApi, filter: TileFindAll, options: Omit<UseInfiniteQueryOptions<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Tile>, TError, TData>, "queryFn" | "queryKey"  | "getPreviousPageParam" | "getNextPageParam" | "initialPageParam"> = {}) => infiniteQueryOptions(
{
    queryKey: [`tile`, `list`, filter ],
    queryFn: ({ pageParam, signal }) => {
        return openApiModuleApi.tile.findAll({ ...filter, page: pageParam, overrides: { signal } })
    },
    staleTime: 2 * 1000,     ...options,
    ...createPaginationOptions()});

export function useInfiniteTileFindAll<TData = InfiniteData<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Tile>>, TError = Error>(filter: TileFindAll, options: Omit<UseInfiniteQueryOptions<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Tile>, TError, TData>, "queryFn" | "queryKey"  | "getPreviousPageParam" | "getNextPageParam" | "initialPageParam"> = {}) {
    const { openApiModuleApi } = useApi();
    return useInfiniteQuery<io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Tile>, TError, TData>(queryOptionTileFindAll(openApiModuleApi, filter, options));
}

/**
  * [GET] /tile/tile/{id}
  */
  export const queryOptionTileFindOne= <TData = io.rocketbase.commons.openapi.sample.dto.Tile, TError = Error>(openApiModuleApi:OpenApiModuleApi, filter: TileFindOne, options: Omit<UseQueryOptions<io.rocketbase.commons.openapi.sample.dto.Tile, TError, TData>, "queryFn" | "queryKey"> = {}) => queryOptions(
{
      queryKey: [`tile`, `detail`, `${filter.id}`, filter ],
      queryFn: ({ signal }) => openApiModuleApi.tile.findOne({ ...filter, overrides: { signal }}),
      staleTime: 2 * 1000, ...options
});

export function useQueryTileFindOne<TData = io.rocketbase.commons.openapi.sample.dto.Tile, TError = Error>(filter: TileFindOne, options: Omit<UseQueryOptions<io.rocketbase.commons.openapi.sample.dto.Tile, TError, TData>, "queryFn" | "queryKey"> = {}) {
    const { openApiModuleApi } = useApi();
    return useQuery<io.rocketbase.commons.openapi.sample.dto.Tile, TError, TData>(queryOptionTileFindOne(openApiModuleApi,filter, options));
}
  
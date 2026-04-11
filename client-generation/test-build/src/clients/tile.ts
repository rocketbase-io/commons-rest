/* eslint-disable no-use-before-define */
// generated 2026-04-11 21:39:05
import { buildRequestorFactory, type RequestorBuilder, type PageableResult } from "@rocketbase/commons-rest-client";
import type { PageableRequest } from "../../model";
import type { io.rocketbase.commons.dto.PageableResult, io.rocketbase.commons.openapi.sample.dto.Tile, UserPreference } from "../../model";
import type { AxiosInstance, AxiosRequestConfig } from "axios";


export interface TileFindAll  extends PageableRequest  {
  /*
   * freetext search, tries to find phrase in any of the tile name, description
   */
  query?: string;
  /*
   * filters by tiletype
   */
  tileType?: string;
  /*
   * filters by labels
   */
  labels?: string[];
  /*
   * filters by sharelevel
   */
  shareLevel?: string;
  /*
   * filters by userPreference
   */
  userPreference?: UserPreference;
  /*
   * filters by categoryId
   */
  categoryId?: number[];
}
export interface TileFindOne  {
  /*
   * id of tile
   */
  id: string;
}


export interface TileApi {
  /*
   * list all tiles (pinboards, searchconfigs, briefings or showrooms a user has access to
   */
  findAll: RequestorBuilder<TileFindAll, io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Tile>>;
  findOne: RequestorBuilder<TileFindOne, io.rocketbase.commons.openapi.sample.dto.Tile>;
}


export function createTileApi(client?: AxiosInstance, cf?: AxiosRequestConfig): TileApi {
  const builder = buildRequestorFactory(client, cf, {
    baseURL: `${cf?.baseURL ?? ""}/test`,
  });

    const findAll: TileApi["findAll"] = builder({
    method: "get",
    url: `/tile`,
    params: ({ page, pageSize, sort, query, tileType, labels, shareLevel, userPreference, categoryId }) => ({ page, pageSize, sort, query, tileType, labels, shareLevel, userPreference, categoryId })
   });
    const findOne: TileApi["findOne"] = builder({
    method: "get",
    url: ({ id }) => `/tile/tile/${id}`
   });

  return {
    findAll, 
    findOne
  };
}
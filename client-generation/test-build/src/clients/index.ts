/* eslint-disable no-use-before-define */
// generated 2026-04-11 21:39:05
import type { AxiosInstance, AxiosRequestConfig } from "axios";

import { type ActivityApi, createActivityApi  } from "./activity";
import { type PermissionApi, createPermissionApi  } from "./permission";
import { type TileApi, createTileApi  } from "./tile";
import { type DownloadApi, createDownloadApi  } from "./download";

export * from "./activity";
export * from "./permission";
export * from "./tile";
export * from "./download";


export interface OpenApiModuleApi {
  activity: ActivityApi;
  permission: PermissionApi;
  tile: TileApi;
  download: DownloadApi;
}

export function createOpenApiModuleApi(client?: AxiosInstance, cf?: AxiosRequestConfig): OpenApiModuleApi {
  const activity= createActivityApi(client, cf);
  const permission= createPermissionApi(client, cf);
  const tile= createTileApi(client, cf);
  const download= createDownloadApi(client, cf);

  return {
    activity, 
    permission, 
    tile, 
    download
  };
}
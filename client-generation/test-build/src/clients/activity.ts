/* eslint-disable no-use-before-define */
// generated 2026-04-11 21:39:05
import { buildRequestorFactory, type RequestorBuilder, type PageableResult } from "@rocketbase/commons-rest-client";
import type { PageableRequest } from "../../model";
import type { io.rocketbase.commons.dto.PageableResult, io.rocketbase.commons.openapi.sample.dto.Activity } from "../../model";
import type { AxiosInstance, AxiosRequestConfig } from "axios";


export interface ActivityFindAll  extends PageableRequest  {
  /*
   * freetext search, tries to find phrase in any of the activity name
   */
  query?: string;
  /*
   * filters by activity type
   */
  activityType?: string;
}
export interface ActivityFindById  {
  id: string;
}


export interface ActivityApi {
  /*
   * list all tiles (pinboards, searchconfigs, briefings or showrooms a user has access to
   */
  findAll: RequestorBuilder<ActivityFindAll, io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>>;
  findById: RequestorBuilder<ActivityFindById, io.rocketbase.commons.openapi.sample.dto.Activity>;
  getAll: RequestorBuilder<PageableRequest, io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.openapi.sample.dto.Activity>>;
}


export function createActivityApi(client?: AxiosInstance, cf?: AxiosRequestConfig): ActivityApi {
  const builder = buildRequestorFactory(client, cf, {
    baseURL: `${cf?.baseURL ?? ""}/test`,
  });

    const findAll: ActivityApi["findAll"] = builder({
    method: "get",
    url: `/activity`,
    params: ({ page, pageSize, sort, query, activityType }) => ({ page, pageSize, sort, query, activityType })
   });
    const findById: ActivityApi["findById"] = builder({
    method: "get",
    url: ({ id }) => `/activity/${id}`
   });
    const getAll: ActivityApi["getAll"] = builder({
    method: "get",
    url: `/activity/validate/logs`,
    params: ({ page, pageSize, sort }) => ({ page, pageSize, sort })
   });

  return {
    findAll, 
    findById, 
    getAll, 
  };
}
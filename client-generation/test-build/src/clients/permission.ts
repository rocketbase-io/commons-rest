/* eslint-disable no-use-before-define */
// generated 2026-04-11 21:39:05
import { buildRequestorFactory, type RequestorBuilder, type PageableResult } from "@rocketbase/commons-rest-client";
import type { PageableRequest } from "../../model";
import type { PermissionCmd } from "../../model";
import type { AxiosInstance, AxiosRequestConfig } from "axios";


export interface PermissionSetPermission  {
  body: PermissionCmd;
}


export interface PermissionApi {
  /*
   * add/update permission for a set of identities
   */
  setPermission: RequestorBuilder<PermissionSetPermission, void>;
}


export function createPermissionApi(client?: AxiosInstance, cf?: AxiosRequestConfig): PermissionApi {
  const builder = buildRequestorFactory(client, cf, {
    baseURL: `${cf?.baseURL ?? ""}/test`,
  });

    const setPermission: PermissionApi["setPermission"] = builder({
    method: "put",
    url: `/element/set-permission`,
    body: ({ body }) => body
   });

  return {
    setPermission
  };
}
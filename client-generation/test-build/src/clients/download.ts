/* eslint-disable no-use-before-define */
// generated 2026-04-11 21:39:05
import { buildRequestorFactory, type RequestorBuilder, type PageableResult } from "@rocketbase/commons-rest-client";
import type { PageableRequest } from "../../model";
import type { java.io.OutputStream, org.springframework.core.io.Resource } from "../../model";
import type { AxiosInstance, AxiosRequestConfig } from "axios";




export interface DownloadApi {
  downloadPpt: RequestorBuilder<unknown, java.io.OutputStream>;
  downloadPdf: RequestorBuilder<unknown, org.springframework.core.io.Resource>;
  downloadExcel: RequestorBuilder<unknown, number[]>;
}


export function createDownloadApi(client?: AxiosInstance, cf?: AxiosRequestConfig): DownloadApi {
  const builder = buildRequestorFactory(client, cf, {
    baseURL: `${cf?.baseURL ?? ""}/test`,
  });

    const downloadPpt: DownloadApi["downloadPpt"] = builder({
    method: "get",
    url: `/download/ppt`
   });
    const downloadPdf: DownloadApi["downloadPdf"] = builder({
    method: "get",
    url: `/download/pdf`
   });
    const downloadExcel: DownloadApi["downloadExcel"] = builder({
    method: "get",
    url: `/download/excel`
   });

  return {
    downloadPpt, 
    downloadPdf, 
    downloadExcel
  };
}
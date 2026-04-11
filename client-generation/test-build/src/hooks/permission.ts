/* eslint-disable no-use-before-define */
// generated 2026-04-11 21:39:05
import type { PermissionSetPermission } from "../../clients";
import type { OpenApiModuleApi } from "../../clients";import { useInfiniteQuery, useQuery, useMutation, useQueryClient, type UseQueryOptions, type UseInfiniteQueryOptions, type UseMutationOptions , type InfiniteData, queryOptions, infiniteQueryOptions  } from "@tanstack/react-query";
import type { PageableResult } from "@rocketbase/commons-rest-client";
import { useApi } from "../../clients";
import type { PageableRequest } from "../../model";
import { createPaginationOptions } from "../util";
import type { PermissionCmd } from "../../model";



/**
  * [PUT] /element/set-permission
  * add/update permission for a set of identities
  */
  export function useMutationPermissionSetPermission<TData = void, TError = Error, TVariables = PermissionSetPermission, TContext = unknown>({onSuccess, ...options}: Omit<UseMutationOptions<void, TError, PermissionSetPermission, TContext>,'mutationFn'> = {}) {
  const queryClient = useQueryClient();
  const { openApiModuleApi } = useApi();
  return useMutation<void, TError, PermissionSetPermission, TContext>({
    mutationFn: (content: PermissionSetPermission) => openApiModuleApi.permission.setPermission( content ),
            onSuccess: async (data: void, variables: PermissionSetPermission, onMutateResult, context) => {
            const invalide = async () => await Promise.all([        queryClient.invalidateQueries(
            {queryKey: [`element`, `detail`, `${variables.body.objectId}`]}
                   ),         queryClient.invalidateQueries(
            {queryKey: [`activities`, `${variables.body.objectId}`]}
                   )]);

        if (onSuccess) {
                    const result = onSuccess(data, variables, onMutateResult, context);
                    if (result && Object.prototype.toString.call(result) === "[object Promise]") {
            await result;
            return invalide();
          }
        }
        return invalide();
    },
        ...options
  });
}
  
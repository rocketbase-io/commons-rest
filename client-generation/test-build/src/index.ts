import { createOpenApiModuleApi} from "./clients";
import * as React from "react";
import { useAuth } from "@rocketbase/commons-rest-client";

export * from "./clients";
export * from "./model";
export * from "./hooks";

export const useApi = () => {
const { axiosClient, baseUrl} = useAuth();

return React.useMemo(
() => ({
openApiModuleApi: createOpenApiModuleApi(axiosClient, { baseURL: baseUrl })
}),
[axiosClient]
);
};


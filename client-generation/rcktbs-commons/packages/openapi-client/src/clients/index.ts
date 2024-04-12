import { createOpenApiModuleApi } from './openapi';

import * as React from 'react';
import { useAuth } from '@rocketbase/commons-core';

export * from './openapi';

export const useApi = () => {
  const { axiosClient, baseUrl } = useAuth();

  return React.useMemo(
    () => ({
      openApiModuleApi: createOpenApiModuleApi(axiosClient, { baseURL: baseUrl() }),
    }),
    [axiosClient, baseUrl]
  );
};

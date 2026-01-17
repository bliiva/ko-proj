import axios from 'axios';
import { APP_CONFIG } from '@/constants';

export default (options = {}) => {
  const { useExceptionInterceptor = true } = options || {};
  const http = axios.create({
    baseURL: APP_CONFIG.apiUrl,
    withCredentials: true,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  });
  // http.interceptors.response.use(null, useExceptionInterceptor && exceptionInterceptor);
  return http;
};

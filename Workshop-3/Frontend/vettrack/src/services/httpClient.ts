// src/services/httpClient.ts
import axios, { type AxiosInstance } from "axios";
import { toast } from "vue-sonner";
import { normalizeHttpError } from "../lib/httpError";

let unauthorizedHandler: (() => void) | null = null;

export function setUnauthorizedHandler(handler: (() => void) | null) {
  unauthorizedHandler = handler;
}

function attachInterceptors(instance: AxiosInstance): AxiosInstance {
  instance.interceptors.response.use(
    (response) => response,
    (error) => {
      const { status, title, description } = normalizeHttpError(error);

      // Skip validation spam if you want
      if (status !== 422) {
        toast.error(title, {
          description,
        });
      }

      if (status === 401 && unauthorizedHandler) {
        unauthorizedHandler();
      }

      return Promise.reject(error);
    }
  );

  return instance;
}

// Auth service (FastAPI auth)
export const authHttp = attachInterceptors(
  axios.create({
    baseURL: import.meta.env.VITE_AUTH_API_BASE_URL ?? "http://localhost:8000",
    headers: {
      "Content-Type": "application/json",
    },
  })
);

// Business/core service (Kotlin API)
export const coreHttp = attachInterceptors(
  axios.create({
    baseURL:
      import.meta.env.VITE_BUSINESS_API_BASE_URL ?? "http://localhost:8080",
    headers: {
      "Content-Type": "application/json",
    },
  })
);

// Helper to inject/remove Authorization on the core API
export function setCoreSessionToken(token: string | null) {
  if (token) {
    coreHttp.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  } else {
    delete coreHttp.defaults.headers.common["Authorization"];
  }
}

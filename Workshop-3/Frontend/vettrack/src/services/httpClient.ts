// src/services/httpClient.ts
import axios from "axios";
import { toast } from "vue-sonner";
import { normalizeHttpError } from "../lib/httpError";

export const authHttp = axios.create({
  baseURL: import.meta.env.VITE_AUTH_API_BASE_URL ?? "http://localhost:8000",
  headers: {
    "Content-Type": "application/json",
  },
});

authHttp.interceptors.response.use(
  (response) => response,
  (error) => {
    const { status, title, description } = normalizeHttpError(error);

    // Avoid spamming
    if (status !== 422) {
      toast.error(title, {
        description,
      });
    }

    // Always reject so calling code can handle specifics if needed
    return Promise.reject(error);
  }
);

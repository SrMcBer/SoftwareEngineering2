// src/services/authApi.ts
import { authHttp } from "./httpClient";
import type {
  UserRegisterRequest,
  UserRegisterResponse,
  LoginRequest,
  LoginResponse,
  ChangePasswordRequest,
  MessageResponse,
  UserInfo,
} from "../types/auth";

const AUTH_TOKEN_PREFIX = "Bearer ";

export const authApi = {
  async register(payload: UserRegisterRequest) {
    const { data } = await authHttp.post<UserRegisterResponse>(
      "/register",
      payload
    );
    return data;
  },

  async login(payload: LoginRequest, clientType: string | null = "web") {
    const { data } = await authHttp.post<LoginResponse>("/login", payload, {
      headers: clientType ? { "X-Client-Type": clientType } : undefined,
    });
    return data;
  },

  async logout(sessionToken: string) {
    const { data } = await authHttp.post<MessageResponse>("/logout", null, {
      headers: {
        Authorization: AUTH_TOKEN_PREFIX + sessionToken,
      },
    });
    return data;
  },

  async getMe(sessionToken: string) {
    const { data } = await authHttp.get<UserInfo>("/me", {
      headers: {
        Authorization: AUTH_TOKEN_PREFIX + sessionToken,
      },
    });
    return data;
  },

  async changePassword(sessionToken: string, payload: ChangePasswordRequest) {
    const { data } = await authHttp.post<MessageResponse>(
      "/password/change",
      payload,
      {
        headers: {
          Authorization: AUTH_TOKEN_PREFIX + sessionToken,
        },
      }
    );
    return data;
  },
};

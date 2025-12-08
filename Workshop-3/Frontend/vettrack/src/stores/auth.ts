// src/stores/auth.ts
import { defineStore } from "pinia";
import { authApi } from "../services/authApi";
import type {
  UserInfo,
  LoginRequest,
  UserRegisterRequest,
  ChangePasswordRequest,
} from "../types/auth";
import router from "../router"; // adjust path if needed

interface AuthState {
  user: UserInfo | null;
  sessionToken: string | null;
  loading: boolean;
  error: string | null;
}

const STORAGE_KEY = "vettrack_auth";

export const useAuthStore = defineStore("auth", {
  state: (): AuthState => ({
    user: null,
    sessionToken: null,
    loading: false,
    error: null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.sessionToken && !!state.user,
  },

  actions: {
    // Call this once on app start (e.g. in App.vue or main.ts)
    async initFromStorage() {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return;

      try {
        const parsed = JSON.parse(raw) as {
          user: UserInfo;
          sessionToken: string;
        };
        this.user = parsed.user;
        this.sessionToken = parsed.sessionToken;

        // Optional: verify token by hitting /me
        const freshUser = await authApi.getMe(this.sessionToken);
        this.user = freshUser;
        this.persist();
      } catch {
        this.clear();
      }
    },

    persist() {
      if (this.user && this.sessionToken) {
        localStorage.setItem(
          STORAGE_KEY,
          JSON.stringify({
            user: this.user,
            sessionToken: this.sessionToken,
          })
        );
      } else {
        localStorage.removeItem(STORAGE_KEY);
      }
    },

    clear() {
      this.user = null;
      this.sessionToken = null;
      this.error = null;
      localStorage.removeItem(STORAGE_KEY);
    },

    async login(payload: LoginRequest) {
      this.loading = true;
      this.error = null;
      try {
        const { session_token, user } = await authApi.login(payload);
        this.sessionToken = session_token;
        this.user = user;
        this.persist();
        // navigate to home if you like
        await router.push("/");
      } catch (err: any) {
        this.error =
          err?.response?.data?.message ||
          err?.message ||
          "Error while logging in";
        throw err;
      } finally {
        this.loading = false;
      }
    },

    async register(payload: UserRegisterRequest) {
      this.loading = true;
      this.error = null;
      try {
        await authApi.register(payload);
        // After registering, auto-login:
        await this.login({
          email: payload.email,
          password: payload.password,
        });
      } catch (err: any) {
        this.error =
          err?.response?.data?.message ||
          err?.message ||
          "Error while registering";
        throw err;
      } finally {
        this.loading = false;
      }
    },

    async logout() {
      if (!this.sessionToken) {
        this.clear();
        await router.push("/login");
        return;
      }

      this.loading = true;
      this.error = null;
      try {
        await authApi.logout(this.sessionToken);
      } catch {
        // even if logout fails on server, clear local state
      } finally {
        this.clear();
        this.loading = false;
        await router.push("/login");
      }
    },

    async changePassword(payload: ChangePasswordRequest) {
      if (!this.sessionToken) {
        throw new Error("Not authenticated");
      }
      this.loading = true;
      this.error = null;
      try {
        await authApi.changePassword(this.sessionToken, payload);
      } catch (err: any) {
        this.error =
          err?.response?.data?.message ||
          err?.message ||
          "Error while changing password";
        throw err;
      } finally {
        this.loading = false;
      }
    },
  },
});

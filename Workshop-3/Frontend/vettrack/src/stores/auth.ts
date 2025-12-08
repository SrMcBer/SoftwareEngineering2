import { defineStore } from "pinia";
import axios from "axios";

// Configure axios base URL - update this to match your FastAPI backend
const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8000";
export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: null,
    token: localStorage.getItem("token") || null,
    loading: false,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
  },

  actions: {
    async login(credentials) {
      this.loading = true;
      try {
        // Adjust the endpoint and payload format based on your FastAPI schema
        const response = await axios.post(`${API_URL}/login`, credentials);

        this.token = response.data.session_token;
        this.user = response.data.user;

        localStorage.setItem("token", this.token);

        // Set default authorization header
        axios.defaults.headers.common["Authorization"] = `Bearer ${this.token}`;

        return { success: true };
      } catch (error) {
        console.error("Login error:", error);
        return {
          success: false,
          error:
            error.response?.data?.detail || "Login failed. Please try again.",
        };
      } finally {
        this.loading = false;
      }
    },

    async register(userData) {
      this.loading = true;
      try {
        // Adjust the endpoint and payload format based on your FastAPI schema
        const response = await axios.post(`${API_URL}/auth/register`, userData);

        // Optionally auto-login after registration
        this.token = response.data.access_token;
        this.user = response.data.user;

        localStorage.setItem("token", this.token);
        axios.defaults.headers.common["Authorization"] = `Bearer ${this.token}`;

        return { success: true };
      } catch (error) {
        console.error("Registration error:", error);
        return {
          success: false,
          error:
            error.response?.data?.detail ||
            "Registration failed. Please try again.",
        };
      } finally {
        this.loading = false;
      }
    },

    logout() {
      this.user = null;
      this.token = null;
      localStorage.removeItem("token");
      delete axios.defaults.headers.common["Authorization"];
    },

    // Initialize auth state from localStorage
    initAuth() {
      if (this.token) {
        axios.defaults.headers.common["Authorization"] = `Bearer ${this.token}`;
        // Optionally fetch current user data here
        // this.fetchCurrentUser()
      }
    },

    async fetchCurrentUser() {
      try {
        const response = await axios.get(`${API_URL}/auth/me`);
        this.user = response.data;
      } catch (error) {
        console.error("Failed to fetch user:", error);
        this.logout();
      }
    },
  },
});

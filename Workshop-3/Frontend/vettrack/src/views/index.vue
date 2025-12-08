<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Navbar -->
    <nav class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center">
            <h1
              class="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent"
            >
              My App
            </h1>
          </div>

          <div class="flex items-center gap-4">
            <span class="text-sm text-gray-600">
              {{ authStore.user?.email || "User" }}
            </span>
            <button
              @click="handleLogout"
              class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Welcome Section -->
      <div class="mb-8">
        <h2 class="text-3xl font-bold text-gray-900">Welcome back!</h2>
        <p class="text-gray-600 mt-2">
          You're successfully logged in to your dashboard.
        </p>
      </div>

      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div
          class="bg-white rounded-lg shadow-sm p-6 border border-gray-200 hover:shadow-md transition-shadow"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">Total Users</p>
              <p class="text-2xl font-bold text-gray-900 mt-2">2,543</p>
            </div>
            <div
              class="w-12 h-12 bg-indigo-100 rounded-lg flex items-center justify-center"
            >
              <svg
                class="w-6 h-6 text-indigo-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
                />
              </svg>
            </div>
          </div>
          <p class="text-xs text-green-600 mt-2">↑ 12% from last month</p>
        </div>

        <div
          class="bg-white rounded-lg shadow-sm p-6 border border-gray-200 hover:shadow-md transition-shadow"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">Revenue</p>
              <p class="text-2xl font-bold text-gray-900 mt-2">$45,231</p>
            </div>
            <div
              class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center"
            >
              <svg
                class="w-6 h-6 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
          </div>
          <p class="text-xs text-green-600 mt-2">↑ 8% from last month</p>
        </div>

        <div
          class="bg-white rounded-lg shadow-sm p-6 border border-gray-200 hover:shadow-md transition-shadow"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">Active Projects</p>
              <p class="text-2xl font-bold text-gray-900 mt-2">12</p>
            </div>
            <div
              class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center"
            >
              <svg
                class="w-6 h-6 text-purple-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
            </div>
          </div>
          <p class="text-xs text-gray-600 mt-2">3 completed this week</p>
        </div>

        <div
          class="bg-white rounded-lg shadow-sm p-6 border border-gray-200 hover:shadow-md transition-shadow"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600">Tasks Done</p>
              <p class="text-2xl font-bold text-gray-900 mt-2">234</p>
            </div>
            <div
              class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center"
            >
              <svg
                class="w-6 h-6 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
                />
              </svg>
            </div>
          </div>
          <p class="text-xs text-green-600 mt-2">↑ 23% from last month</p>
        </div>
      </div>

      <!-- Feature Cards Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        <div
          class="bg-white rounded-lg shadow-sm p-6 border border-gray-200 hover:shadow-md transition-shadow cursor-pointer"
        >
          <div class="flex items-center mb-4">
            <div
              class="w-10 h-10 bg-indigo-100 rounded-lg flex items-center justify-center mr-3"
            >
              <span class="text-2xl">📊</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-900">Analytics</h3>
          </div>
          <p class="text-gray-600 text-sm">
            View detailed analytics and insights about your performance.
          </p>
        </div>

        <div
          class="bg-white rounded-lg shadow-sm p-6 border border-gray-200 hover:shadow-md transition-shadow cursor-pointer"
        >
          <div class="flex items-center mb-4">
            <div
              class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center mr-3"
            >
              <span class="text-2xl">⚙️</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-900">Settings</h3>
          </div>
          <p class="text-gray-600 text-sm">
            Manage your account settings and preferences.
          </p>
        </div>

        <div
          class="bg-white rounded-lg shadow-sm p-6 border border-gray-200 hover:shadow-md transition-shadow cursor-pointer"
        >
          <div class="flex items-center mb-4">
            <div
              class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center mr-3"
            >
              <span class="text-2xl">📝</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-900">Content</h3>
          </div>
          <p class="text-gray-600 text-sm">
            Create and manage your content efficiently.
          </p>
        </div>

        <div
          class="bg-white rounded-lg shadow-sm p-6 border border-gray-200 hover:shadow-md transition-shadow cursor-pointer"
        >
          <div class="flex items-center mb-4">
            <div
              class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center mr-3"
            >
              <span class="text-2xl">👥</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-900">Team</h3>
          </div>
          <p class="text-gray-600 text-sm">
            Manage your team members and permissions.
          </p>
        </div>

        <div
          class="bg-white rounded-lg shadow-sm p-6 border border-gray-200 hover:shadow-md transition-shadow cursor-pointer"
        >
          <div class="flex items-center mb-4">
            <div
              class="w-10 h-10 bg-yellow-100 rounded-lg flex items-center justify-center mr-3"
            >
              <span class="text-2xl">📈</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-900">Reports</h3>
          </div>
          <p class="text-gray-600 text-sm">
            Generate and export detailed reports.
          </p>
        </div>

        <div
          class="bg-white rounded-lg shadow-sm p-6 border border-gray-200 hover:shadow-md transition-shadow cursor-pointer"
        >
          <div class="flex items-center mb-4">
            <div
              class="w-10 h-10 bg-pink-100 rounded-lg flex items-center justify-center mr-3"
            >
              <span class="text-2xl">🔔</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-900">Notifications</h3>
          </div>
          <p class="text-gray-600 text-sm">
            Stay updated with real-time notifications.
          </p>
        </div>
      </div>

      <!-- API Integration Notice -->
      <div
        class="bg-gradient-to-r from-indigo-50 to-purple-50 border border-indigo-200 rounded-lg p-6"
      >
        <div class="flex items-start">
          <div
            class="w-10 h-10 bg-indigo-100 rounded-lg flex items-center justify-center mr-4 flex-shrink-0"
          >
            <svg
              class="w-6 h-6 text-indigo-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M13 10V3L4 14h7v7l9-11h-7z"
              />
            </svg>
          </div>
          <div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">
              Ready for FastAPI Integration
            </h3>
            <p class="text-gray-600 text-sm">
              This dashboard is ready to connect to your FastAPI backend. Update
              the API endpoints in the auth store and add your custom components
              to fetch and display real data.
            </p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const authStore = useAuthStore();

const handleLogout = () => {
  authStore.logout();
  router.push("/login");
};
</script>

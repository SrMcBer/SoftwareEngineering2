<script setup lang="ts">
import { useAuthStore } from "../stores/auth";
import { Button } from "@/components/ui/button";

const authStore = useAuthStore();

const onLogout = () => {
  authStore.logout();
};
</script>

<template>
  <div class="p-4 space-y-4">
    <header class="flex items-center justify-between">
      <div>
        <p class="text-sm text-muted-foreground">Logged in as</p>
        <p class="font-semibold">
          {{ authStore.user?.name }} ({{ authStore.user?.email }})
        </p>
      </div>

      <Button variant="outline" @click="onLogout"> Logout </Button>
    </header>

    <!-- rest of your VetTrack dashboard goes here -->
    <main>
      <h1 class="text-2xl font-bold mb-2">VetTrack Dashboard</h1>
      <!-- Patients, visits, etc. will be wired to the core API next -->
    </main>
  </div>
</template>

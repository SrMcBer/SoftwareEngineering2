<script setup lang="ts">
import { onMounted } from "vue";
import { useAuthStore } from "./stores/auth";
import { useTaxonomyStore } from "./stores/taxonomy";

// sonner styles + component
import "vue-sonner/style.css";
import { Toaster } from "@/components/ui/sonner";

const authStore = useAuthStore();
const taxonomyStore = useTaxonomyStore();

onMounted(() => {
  authStore.initFromStorage();
  taxonomyStore.init();
});
</script>

<template>
  <div class="min-h-screen bg-background text-foreground">
    <router-view />
    <Toaster position="top-right" rich-colors close-button expand />
  </div>
</template>

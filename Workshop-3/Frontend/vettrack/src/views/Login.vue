<script setup lang="ts">
import { ref } from "vue";
import { useAuthStore } from "../stores/auth";
import { useRouter } from "vue-router";
import type { LoginRequest } from "../types/auth";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from "@/components/ui/card";

const authStore = useAuthStore();
const router = useRouter();

const form = ref<LoginRequest>({
  email: "",
  password: "",
});

const error = ref<string | null>(null);

const onSubmit = async () => {
  error.value = null;
  try {
    await authStore.login(form.value);
    // router push happens in store, but you can keep it here if you want
    // await router.push("/");
  } catch (e: any) {
    error.value = authStore.error ?? "Login failed";
  }
};
</script>

<template>
  <div class="min-h-screen flex items-center justify-center">
    <Card class="w-full max-w-md">
      <CardHeader>
        <CardTitle>Login</CardTitle>
      </CardHeader>

      <CardContent>
        <form class="space-y-4" @submit.prevent="onSubmit">
          <div class="space-y-2">
            <Label for="email">Email</Label>
            <Input
              id="email"
              type="email"
              v-model="form.email"
              required
              autocomplete="email"
            />
          </div>

          <div class="space-y-2">
            <Label for="password">Password</Label>
            <Input
              id="password"
              type="password"
              v-model="form.password"
              required
              autocomplete="current-password"
            />
          </div>

          <p v-if="error" class="text-sm text-red-500">
            {{ error }}
          </p>

          <Button
            type="submit"
            class="w-full"
            :disabled="authStore.loading"
          >
            <span v-if="authStore.loading">Logging in...</span>
            <span v-else>Login</span>
          </Button>
        </form>
      </CardContent>

      <CardFooter class="text-sm text-muted-foreground">
        <span>Don’t have an account?</span>
        <Button variant="link" class="px-1" @click="router.push('/register')">
          Register
        </Button>
      </CardFooter>
    </Card>
  </div>
</template>

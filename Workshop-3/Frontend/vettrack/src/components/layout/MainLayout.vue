<script setup lang="ts">
import { computed, ref } from "vue";
import { useAuthStore } from "../../stores/auth";
// import { useRouter } from "vue-router";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { toast } from "vue-sonner";

import type { ChangePasswordRequest } from "../../types/auth";

const authStore = useAuthStore();
// const router = useRouter();

const isChangePasswordOpen = ref(false);
const form = ref({
  current_password: "",
  new_password: "",
  confirm_password: "",
});
const localError = ref<string | null>(null);

const userName = computed(() => authStore.user?.name ?? "Unknown user");
const userEmail = computed(() => authStore.user?.email ?? "");
const initials = computed(() => {
  const parts = userName.value.trim().split(" ");
  const first = parts[0]?.[0] ?? "";
  const last = parts[1]?.[0] ?? "";
  return (first + last).toUpperCase() || "VT";
});

const isSubmitting = computed(() => authStore.loading);

async function onLogout() {
  await authStore.logout();
}

function openChangePassword() {
  localError.value = null;
  form.value.current_password = "";
  form.value.new_password = "";
  form.value.confirm_password = "";
  isChangePasswordOpen.value = true;
}

async function submitChangePassword() {
  localError.value = null;

  if (!form.value.current_password || !form.value.new_password) {
    localError.value = "Please fill out all fields.";
    return;
  }

  if (form.value.new_password !== form.value.confirm_password) {
    localError.value = "Passwords do not match.";
    return;
  }

  const payload: ChangePasswordRequest = {
    current_password: form.value.current_password,
    new_password: form.value.new_password,
  };

  try {
    await authStore.changePassword(payload);
    // changePassword already logs out and redirects
    toast.success("Password updated", {
      description: "Please log in again with your new password.",
    });
    isChangePasswordOpen.value = false;
  } catch {
    // Error toast already comes from axios interceptor; show inline too if we have store.error
    if (authStore.error) {
      localError.value = authStore.error;
    } else {
      localError.value = "Failed to change password.";
    }
  }
}
</script>

<template>
  <div class="min-h-screen flex flex-col bg-muted/30">
    <!-- Top bar -->
    <header
      class="h-16 px-6 border-b bg-background flex items-center justify-between"
    >
      <div class="flex items-center gap-2">
        <div
          class="h-9 w-9 rounded-full bg-emerald-500 flex items-center justify-center text-white font-semibold"
        >
          VT
        </div>
        <span class="font-semibold text-lg">VetTrack</span>
      </div>

      <div class="flex items-center gap-3">
        <div class="hidden sm:flex flex-col items-end">
          <span class="font-medium leading-tight">
            {{ userName }}
          </span>
          <span class="text-xs text-muted-foreground leading-tight">
            {{ userEmail }}
          </span>
        </div>

        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <button
              class="inline-flex items-center justify-center rounded-full focus:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <Avatar class="h-9 w-9">
                <AvatarFallback>{{ initials }}</AvatarFallback>
              </Avatar>
            </button>
          </DropdownMenuTrigger>

          <DropdownMenuContent align="end" class="w-48">
            <DropdownMenuLabel>Account</DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem @click="openChangePassword">
              Change password
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem class="text-red-600" @click="onLogout">
              Log out
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>

    <!-- Main content -->
    <main class="flex-1 px-6 py-6">
      <slot />
    </main>

    <!-- Change password dialog -->
    <Dialog v-model:open="isChangePasswordOpen">
      <DialogContent class="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Change password</DialogTitle>
          <DialogDescription>
            Update your password. You'll be signed out after changing it.
          </DialogDescription>
        </DialogHeader>

        <div class="space-y-4 py-2">
          <div class="space-y-1">
            <Label for="current-password">Current password</Label>
            <Input
              id="current-password"
              type="password"
              v-model="form.current_password"
              autocomplete="current-password"
            />
          </div>

          <div class="space-y-1">
            <Label for="new-password">New password</Label>
            <Input
              id="new-password"
              type="password"
              v-model="form.new_password"
              autocomplete="new-password"
            />
          </div>

          <div class="space-y-1">
            <Label for="confirm-password">Confirm new password</Label>
            <Input
              id="confirm-password"
              type="password"
              v-model="form.confirm_password"
              autocomplete="new-password"
            />
          </div>

          <p v-if="localError" class="text-sm text-red-500">
            {{ localError }}
          </p>
        </div>

        <DialogFooter>
          <Button
            variant="outline"
            @click="isChangePasswordOpen = false"
            :disabled="isSubmitting"
          >
            Cancel
          </Button>
          <Button @click="submitChangePassword" :disabled="isSubmitting">
            <span v-if="isSubmitting">Saving…</span>
            <span v-else>Save and log out</span>
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>

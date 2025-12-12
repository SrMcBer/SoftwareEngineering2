<script setup lang="ts">
import { ref, watch } from "vue";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";

const props = defineProps<{
  open: boolean;
  patientName: string;
}>();

const emit = defineEmits<{
  "update:open": [value: boolean];
  submit: [title: string, dueAt: string];
}>();

const isSaving = ref(false);
const title = ref("");
const dueAt = ref("");

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      // Default due date: 1 hour from now
      const nowLocal = new Date();
      nowLocal.setMinutes(nowLocal.getMinutes() + 60);
      dueAt.value = nowLocal.toISOString().slice(0, 16);
      title.value = "";
    }
  }
);

async function handleSubmit() {
  if (!title.value.trim() || !dueAt.value) {
    return;
  }

  isSaving.value = true;
  try {
    emit("submit", title.value.trim(), dueAt.value);
  } finally {
    isSaving.value = false;
  }
}

function handleClose() {
  if (!isSaving.value) {
    emit("update:open", false);
  }
}
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <DialogTitle>Create reminder</DialogTitle>
        <DialogDescription>
          Create a reminder for {{ patientName }}.
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 py-2">
        <div class="space-y-1">
          <label class="text-xs font-medium text-muted-foreground">
            Title
          </label>
          <Input
            v-model="title"
            placeholder="Recheck, vaccines, lab follow-up..."
          />
        </div>

        <div class="space-y-1">
          <label class="text-xs font-medium text-muted-foreground">
            Due date &amp; time
          </label>
          <input
            v-model="dueAt"
            type="datetime-local"
            class="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm outline-none disabled:cursor-not-allowed disabled:opacity-50"
          />
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="handleClose" :disabled="isSaving">
          Cancel
        </Button>
        <Button @click="handleSubmit" :disabled="isSaving">
          Save reminder
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

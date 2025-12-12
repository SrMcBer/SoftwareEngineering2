<script setup lang="ts">
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { Reminder } from "../../types/business";
import { useFormatting } from "../../composables/useFormatting";

defineProps<{
  reminders: Reminder[];
}>();

const emit = defineEmits<{
  markDone: [reminder: Reminder];
  dismiss: [reminder: Reminder];
}>();

const { formatReminderDue } = useFormatting();
</script>

<template>
  <div class="mt-4">
    <div
      v-if="reminders.length === 0"
      class="text-sm text-muted-foreground italic py-4"
    >
      No reminders for this patient yet.
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="rem in reminders"
        :key="rem.id"
        class="rounded-lg border bg-muted/50 px-4 py-3 text-sm"
      >
        <div
          class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between"
        >
          <div>
            <div class="flex items-center gap-2">
              <span class="font-medium">{{ rem.title }}</span>
              <Badge
                class="text-[10px] uppercase tracking-wide"
                :class="{
                  'bg-emerald-100 text-emerald-800': rem.status === 'done',
                  'bg-amber-100 text-amber-800': rem.status === 'pending',
                  'bg-red-100 text-red-800': rem.status === 'overdue',
                }"
              >
                {{ rem.status }}
              </Badge>
            </div>
            <div class="mt-1 text-xs text-muted-foreground">
              Due {{ formatReminderDue(rem.dueAt) }}
              <span v-if="rem.createdByName">
                • by {{ rem.createdByName }}
              </span>
            </div>
          </div>

          <div class="flex gap-2">
            <Button
              size="sm"
              variant="outline"
              @click="emit('markDone', rem)"
              :disabled="rem.status === 'done'"
            >
              Mark done
            </Button>
            <Button size="sm" variant="ghost" @click="emit('dismiss', rem)">
              Dismiss
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

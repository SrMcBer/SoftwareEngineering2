<script setup lang="ts">
import { ref } from "vue";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import type { Medication, DoseEvent } from "../../types/business";
import { useMedicationHelpers } from "../../composables/useMedicationHelpers";

const props = defineProps<{
  medication: Medication;
  doseEvents: DoseEvent[];
}>();

const emit = defineEmits<{
  doseRecorded: [amount?: string, notes?: string];
}>();

const {
  isMedicationActive,
  getNextDoseMeta,
  getDoseProgress,
  formatDoseDateTime,
} = useMedicationHelpers();

const isRecordingDose = ref(false);
const doseAmount = ref(props.medication.dosage ?? "");
const doseNotes = ref("");

function startRecordDose() {
  isRecordingDose.value = true;
  doseAmount.value = props.medication.dosage ?? "";
  doseNotes.value = "";
}

function submitDose() {
  emit(
    "doseRecorded",
    doseAmount.value || undefined,
    doseNotes.value || undefined
  );
  isRecordingDose.value = false;
}
</script>

<template>
  <div class="rounded-lg border bg-muted/50 px-4 py-3 text-sm">
    <div
      class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between"
    >
      <div class="font-medium">
        {{ medication.name }}
      </div>
      <Badge
        :class="
          isMedicationActive(medication)
            ? 'bg-emerald-100 text-emerald-800'
            : 'bg-slate-100 text-slate-800'
        "
        class="text-xs font-normal"
      >
        {{ isMedicationActive(medication) ? "Active" : "Completed" }}
      </Badge>
    </div>

    <div class="mt-1 text-muted-foreground space-y-1">
      <div v-if="medication.dosage">Dosage: {{ medication.dosage }}</div>
      <div v-if="medication.route">Route: {{ medication.route }}</div>
      <div v-if="medication.frequency">
        Frequency: {{ medication.frequency }}
      </div>

      <div v-if="medication.startDate || medication.endDate" class="text-xs">
        <span v-if="medication.startDate">From {{ medication.startDate }}</span>
        <span v-if="medication.startDate && medication.endDate"> • </span>
        <span v-if="medication.endDate">To {{ medication.endDate }}</span>
      </div>

      <!-- Active-only controls -->
      <div
        v-if="isMedicationActive(medication)"
        class="mt-2 flex flex-wrap items-center gap-2 text-xs"
      >
        <!-- Countdown pill -->
        <div class="flex items-center gap-2">
          <template v-if="getNextDoseMeta(medication)">
            <span
              :class="[
                'inline-flex items-center rounded-full px-2 py-0.5 font-medium',
                getNextDoseMeta(medication)?.isOverdue
                  ? 'bg-red-100 text-red-800 animate-pulse'
                  : getNextDoseMeta(medication)?.isSoon
                  ? 'bg-amber-100 text-amber-800 animate-pulse'
                  : 'bg-emerald-100 text-emerald-800',
              ]"
            >
              <span
                class="mr-1 h-1.5 w-1.5 rounded-full"
                :class="
                  getNextDoseMeta(medication)?.isOverdue
                    ? 'bg-red-500'
                    : getNextDoseMeta(medication)?.isSoon
                    ? 'bg-amber-500'
                    : 'bg-emerald-500'
                "
              />
              next dose {{ getNextDoseMeta(medication)?.label }}
            </span>
          </template>

          <span v-else class="text-muted-foreground">
            No next dose scheduled
          </span>
        </div>

        <!-- Progress bar -->
        <div v-if="getDoseProgress(medication)" class="space-y-1">
          <div
            class="flex justify-between text-[11px] text-muted-foreground gap-2"
          >
            <span>
              Last dose: {{ getDoseProgress(medication)?.lastLabel }}
            </span>
            <span>Next due: {{ getDoseProgress(medication)?.nextLabel }}</span>
          </div>

          <Progress
            :model-value="getDoseProgress(medication)?.percent ?? 0"
            class="h-1.5"
          />
        </div>
      </div>

      <Button size="sm" variant="outline" @click="startRecordDose">
        Record dose
      </Button>
    </div>

    <!-- Dose recording form -->
    <div v-if="isRecordingDose" class="mt-2 border-t pt-2 space-y-2 text-xs">
      <div class="flex flex-col md:flex-row md:items-center md:gap-2">
        <label class="flex-1">
          <span class="block mb-1">Amount</span>
          <input
            v-model="doseAmount"
            type="text"
            class="w-full rounded border px-2 py-1 bg-background"
            placeholder="e.g. 0.5 mL"
          />
        </label>

        <label class="flex-1 mt-2 md:mt-0">
          <span class="block mb-1">Notes</span>
          <input
            v-model="doseNotes"
            type="text"
            class="w-full rounded border px-2 py-1 bg-background"
            placeholder="Optional notes"
          />
        </label>
      </div>

      <div class="flex justify-end gap-2 mt-2">
        <Button size="sm" variant="ghost" @click="isRecordingDose = false">
          Cancel
        </Button>
        <Button size="sm" @click="submitDose"> Save dose </Button>
      </div>
    </div>

    <!-- Dose history -->
    <div class="mt-2 border-t pt-2 text-xs text-muted-foreground">
      <div class="font-medium mb-1">Dose history</div>

      <div v-if="doseEvents.length">
        <ul class="space-y-1 max-h-40 overflow-y-auto">
          <li
            v-for="dose in doseEvents"
            :key="dose.id"
            class="flex justify-between gap-2"
          >
            <span>{{ formatDoseDateTime(dose.occurredAt) }}</span>
            <span v-if="dose.amount">{{ dose.amount }}</span>
            <span v-if="dose.notes" class="italic truncate">
              — {{ dose.notes }}
            </span>
          </li>
        </ul>
      </div>
      <div v-else>No doses recorded yet.</div>
    </div>
  </div>
</template>

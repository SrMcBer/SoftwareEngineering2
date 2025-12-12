<script setup lang="ts">
import MedicationCard from "./MedicationCard.vue";
import type { Medication, DoseEvent } from "../../types/business";

defineProps<{
  medications: Medication[];
  doseEventsByMedId: Record<string, DoseEvent[]>;
}>();

const emit = defineEmits<{
  doseRecorded: [medicationId: string, amount?: string, notes?: string];
}>();

function handleDoseRecorded(medId: string, amount?: string, notes?: string) {
  emit("doseRecorded", medId, amount, notes);
}
</script>

<template>
  <div class="mt-4 space-y-3">
    <div
      v-if="medications.length === 0"
      class="text-sm text-muted-foreground italic py-4"
    >
      No medications recorded.
    </div>

    <MedicationCard
      v-for="med in medications"
      :key="med.id"
      :medication="med"
      :dose-events="doseEventsByMedId[med.id] ?? []"
      @dose-recorded="
        (amount, notes) => handleDoseRecorded(med.id, amount, notes)
      "
    />
  </div>
</template>

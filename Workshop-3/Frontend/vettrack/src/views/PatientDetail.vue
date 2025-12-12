<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRoute, useRouter } from "vue-router";
import { usePatientStore } from "../stores/patientStore";
import MainLayout from "../components/layout/MainLayout.vue";
import PatientHeader from "../components/patients/PatientHeader.vue";
import VisitsTab from "../components/patients/VisitsTab.vue";
import MedicationsTab from "../components/medication/MedicationsTab.vue";
import RemindersTab from "../components/reminder/RemindersTab.vue";
import MedicationCreateDialog from "../components/medication/MedicationCreateDialog.vue";
import ReminderDialog from "../components/reminder/ReminderDialog.vue";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import type { Medication, Reminder } from "../types/business";
import { toast } from "vue-sonner";
import { useMedicationHelpers } from "../composables/useMedicationHelpers";
import { useFormatting } from "../composables/useFormatting";

const route = useRoute();
const router = useRouter();
const patientId = route.params.id as string;

const patientStore = usePatientStore();

// Composables
const { startClock } = useMedicationHelpers();
const { getAgeLabel } = useFormatting();

// Local state
const activeTab = ref<"visits" | "medications" | "reminders">("visits");
const isMedicationDialogOpen = ref(false);
const isReminderDialogOpen = ref(false);

// Clock for real-time updates
let clockTimer: number | undefined;
onMounted(async () => {
  await patientStore.loadPatient(patientId);
  clockTimer = startClock();
});

onBeforeUnmount(() => {
  if (clockTimer != null) {
    window.clearInterval(clockTimer);
  }
  patientStore.clearPatient();
});

// Computed
const ageLabel = getAgeLabel(patientStore.patient);

// Actions
function onNewVisit() {
  toast.info("New Visit", {
    description: "Visit creation flow not implemented yet.",
  });
}

function onAddMedication() {
  isMedicationDialogOpen.value = true;
}

function onCreateReminder() {
  isReminderDialogOpen.value = true;
}

function handleMedicationCreated(med: Medication) {
  patientStore.addMedication(med);
}

async function handleDoseRecorded(
  medicationId: string,
  amount?: string,
  notes?: string
) {
  await patientStore.recordDose(medicationId, amount, notes);
}

async function handleReminderSubmit(title: string, dueAt: string) {
  await patientStore.createReminder(title, dueAt);
  isReminderDialogOpen.value = false;
}

async function handleMarkReminderDone(rem: Reminder) {
  await patientStore.markReminderDone(rem.id);
}

async function handleDismissReminder(rem: Reminder) {
  await patientStore.dismissReminder(rem.id);
}
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <Button variant="ghost" class="px-0 text-sm" @click="router.push('/')">
        ← Back to Patients
      </Button>

      <PatientHeader
        v-if="patientStore.patient"
        :patient="patientStore.patient"
        :owner="patientStore.owner"
        :age-label="ageLabel"
        @new-visit="onNewVisit"
        @add-medication="onAddMedication"
        @create-reminder="onCreateReminder"
      />

      <div
        v-else-if="!patientStore.isLoading"
        class="text-sm text-muted-foreground"
      >
        Patient not found.
      </div>

      <!-- Tabs -->
      <Card v-if="patientStore.patient">
        <CardContent class="pt-6">
          <Tabs v-model="activeTab" default-value="visits" class="w-full">
            <TabsList>
              <TabsTrigger value="visits">Visits</TabsTrigger>
              <TabsTrigger value="medications">Medications</TabsTrigger>
              <TabsTrigger value="reminders">Reminders</TabsTrigger>
            </TabsList>

            <TabsContent value="visits">
              <VisitsTab :visits="patientStore.visits" />
            </TabsContent>

            <TabsContent value="medications">
              <MedicationsTab
                :medications="patientStore.medications"
                :dose-events-by-med-id="patientStore.doseEventsByMedId"
                @dose-recorded="handleDoseRecorded"
              />
            </TabsContent>

            <TabsContent value="reminders">
              <RemindersTab
                :reminders="patientStore.reminders"
                @mark-done="handleMarkReminderDone"
                @dismiss="handleDismissReminder"
              />
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>

    <MedicationCreateDialog
      v-if="patientStore.patient"
      v-model:open="isMedicationDialogOpen"
      :patient-id="patientStore.patient.id"
      @created="handleMedicationCreated"
    />

    <ReminderDialog
      v-if="patientStore.patient"
      v-model:open="isReminderDialogOpen"
      :patient-name="patientStore.patient.name"
      @submit="handleReminderSubmit"
    />
  </MainLayout>
</template>

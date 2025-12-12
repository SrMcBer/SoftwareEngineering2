// stores/patientStore.ts
import { defineStore } from "pinia";
import { ref, computed } from "vue";
import type {
  Patient,
  Owner,
  VisitDetails,
  Medication,
  DoseEvent,
  Reminder,
} from "../types/business";
import {
  patientsApi,
  ownersApi,
  visitsApi,
  medicationsApi,
  remindersApi,
} from "../services/businessApi";
import { toast } from "vue-sonner";

export const usePatientStore = defineStore("patient", () => {
  // State
  const patient = ref<Patient | null>(null);
  const owner = ref<Owner | null>(null);
  const visits = ref<VisitDetails[]>([]);
  const medications = ref<Medication[]>([]);
  const reminders = ref<Reminder[]>([]);
  const doseEventsByMedId = ref<Record<string, DoseEvent[]>>({});
  const isLoading = ref(false);

  // Getters
  const activeMedications = computed(() =>
    medications.value.filter((m) => !isMedicationCompleted(m))
  );

  const completedMedications = computed(() =>
    medications.value.filter((m) => isMedicationCompleted(m))
  );

  const pendingReminders = computed(() =>
    reminders.value.filter(
      (r) => r.status === "pending" || r.status === "overdue"
    )
  );

  // Helper functions
  function isMedicationCompleted(m: Medication): boolean {
    if (!m.endDate) return false;
    const end = new Date(m.endDate);
    const today = new Date();
    end.setHours(23, 59, 59, 999);
    return end.getTime() < today.getTime();
  }

  // Actions
  async function loadPatient(patientId: string) {
    isLoading.value = true;
    try {
      const p = await patientsApi.get(patientId);
      patient.value = p;

      // Load owner
      if (p.ownerId) {
        try {
          owner.value = await ownersApi.get(p.ownerId);
        } catch {
          owner.value = null;
        }
      }

      // Load all related data in parallel
      await Promise.all([
        loadVisits(patientId),
        loadMedications(patientId),
        loadReminders(patientId),
      ]);
    } catch (error) {
      console.error("Failed to load patient", error);
      toast.error("Failed to load patient data");
      throw error;
    } finally {
      isLoading.value = false;
    }
  }

  async function loadVisits(patientId: string) {
    const baseVisits = await visitsApi.listForPatient(patientId);
    const details = await Promise.all(
      baseVisits.map(async (v) => {
        try {
          return await visitsApi.getDetails(v.id);
        } catch {
          return {
            visit: v,
            patient: patient.value!,
            exams: [],
            medications: [],
            attachments: [],
          } as VisitDetails;
        }
      })
    );

    details.sort(
      (a, b) =>
        new Date(b.visit.dateTime).getTime() -
        new Date(a.visit.dateTime).getTime()
    );

    visits.value = details;
  }

  async function loadMedications(patientId: string) {
    const meds = await patientsApi.listMedications(patientId);
    medications.value = meds;

    // Load dose events for each medication
    const doseMap: Record<string, DoseEvent[]> = {};
    await Promise.all(
      meds.map(async (med) => {
        try {
          const events = await medicationsApi.listDoseEvents(med.id);
          doseMap[med.id] = events;
        } catch (err) {
          console.error(
            "Failed to load dose events for medication",
            med.id,
            err
          );
          doseMap[med.id] = [];
        }
      })
    );

    doseEventsByMedId.value = doseMap;
  }

  async function loadReminders(patientId: string) {
    reminders.value = await remindersApi.getRemindersForPatient(patientId);
  }

  async function addMedication(medication: Medication) {
    medications.value = [medication, ...medications.value];
    doseEventsByMedId.value[medication.id] = [];
  }

  async function recordDose(
    medicationId: string,
    amount?: string,
    notes?: string
  ) {
    try {
      const created = await medicationsApi.recordDose(medicationId, {
        amount,
        notes,
      });

      // Update dose events
      doseEventsByMedId.value[medicationId] = [
        created,
        ...(doseEventsByMedId.value[medicationId] ?? []),
      ];

      // Reload medications to get updated nextDueAt
      if (patient.value) {
        await loadMedications(patient.value.id);
      }

      toast.success("Dose recorded");
    } catch (err) {
      console.error("Failed to record dose", err);
      toast.error("Failed to record dose");
      throw err;
    }
  }

  async function createReminder(title: string, dueAt: string) {
    if (!patient.value) return;

    try {
      const isoDueAt = new Date(dueAt).toISOString();

      const created = await remindersApi.createReminder({
        patientId: patient.value.id,
        title,
        dueAt: isoDueAt,
      });

      reminders.value = [created, ...reminders.value];

      toast.success("Reminder created", {
        description: "The reminder was saved successfully.",
      });
    } catch (err) {
      console.error("Failed to create reminder", err);
      toast.error("Could not create reminder", {
        description: "Please try again in a moment.",
      });
      throw err;
    }
  }

  async function markReminderDone(reminderId: string) {
    try {
      const updated = await remindersApi.markReminderDone(reminderId);
      reminders.value = reminders.value.map((r) =>
        r.id === updated.id ? updated : r
      );
      toast.success("Reminder marked as done");
    } catch (err) {
      console.error("Failed to mark reminder as done", err);
      toast.error("Could not mark reminder as done");
      throw err;
    }
  }

  async function dismissReminder(reminderId: string) {
    try {
      const updated = await remindersApi.dismissReminder(reminderId);
      reminders.value = reminders.value.map((r) =>
        r.id === updated.id ? updated : r
      );
      toast.success("Reminder dismissed");
    } catch (err) {
      console.error("Failed to dismiss reminder", err);
      toast.error("Could not dismiss reminder");
      throw err;
    }
  }

  function clearPatient() {
    patient.value = null;
    owner.value = null;
    visits.value = [];
    medications.value = [];
    reminders.value = [];
    doseEventsByMedId.value = {};
  }

  return {
    // State
    patient,
    owner,
    visits,
    medications,
    reminders,
    doseEventsByMedId,
    isLoading,

    // Getters
    activeMedications,
    completedMedications,
    pendingReminders,

    // Actions
    loadPatient,
    addMedication,
    recordDose,
    createReminder,
    markReminderDone,
    dismissReminder,
    clearPatient,
  };
});

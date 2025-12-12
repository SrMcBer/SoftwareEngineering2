// composables/usePatientData.ts
import { ref } from "vue";
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

export function usePatientData(patientId: string) {
  const patient = ref<Patient | null>(null);
  const owner = ref<Owner | null>(null);
  const visits = ref<VisitDetails[]>([]);
  const medications = ref<Medication[]>([]);
  const reminders = ref<Reminder[]>([]);
  const doseEventsByMedId = ref<Record<string, DoseEvent[]>>({});
  const isLoading = ref(true);

  async function loadData() {
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

      // Load visits with details
      const baseVisits = await visitsApi.listForPatient(patientId);
      const details = await Promise.all(
        baseVisits.map(async (v) => {
          try {
            return await visitsApi.getDetails(v.id);
          } catch {
            return {
              visit: v,
              patient: {
                id: p.id,
                ownerId: p.ownerId,
                name: p.name,
                species: p.species,
                breed: p.breed,
                sex: p.sex,
                dob: p.dob,
                color: p.color,
              },
              exams: [],
              medications: [],
              attachments: [],
            } as VisitDetails;
          }
        })
      );

      // Sort visits newest first
      details.sort((a, b) => {
        return (
          new Date(b.visit.dateTime).getTime() -
          new Date(a.visit.dateTime).getTime()
        );
      });
      visits.value = details;

      // Load medications
      const meds = await patientsApi.listMedications(patientId);
      medications.value = meds;

      // Load dose events for each medication
      const doseMap: Record<string, DoseEvent[]> = {};
      for (const med of meds) {
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
      }
      doseEventsByMedId.value = doseMap;

      // Load reminders
      reminders.value = await remindersApi.getRemindersForPatient(patientId);
    } finally {
      isLoading.value = false;
    }
  }

  async function reloadMedications() {
    medications.value = await patientsApi.listMedications(patientId);
  }

  return {
    patient,
    owner,
    visits,
    medications,
    reminders,
    doseEventsByMedId,
    isLoading,
    loadData,
    reloadMedications,
  };
}

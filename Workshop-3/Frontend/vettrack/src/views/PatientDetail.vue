<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRoute, useRouter } from "vue-router";
import MainLayout from "../components/layout/MainLayout.vue";
import MedicationCreateDialog from "../components/medication/MedicationCreateDialog.vue";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Separator } from "@/components/ui/separator";
import { Progress } from "@/components/ui/progress";

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

const route = useRoute();
const router = useRouter();
const patientId = route.params.id as string;

const patient = ref<Patient | null>(null);
const owner = ref<Owner | null>(null);
const visits = ref<VisitDetails[]>([]);
const medications = ref<Medication[]>([]);
const isMedicationDialogOpen = ref(false);

const doseEventsByMedId = ref<Record<string, DoseEvent[]>>({});

const recordingDoseForMedId = ref<string | null>(null);
const doseAmount = ref<string>("");
const doseNotes = ref<string>("");

const now = ref(new Date());
let clockTimer: number | undefined;

const reminders = ref<Reminder[]>([]);

async function loadReminders() {
  reminders.value = await remindersApi.getRemindersForPatient(patientId);
}

async function onCreateReminder(input: { title: string; dueAt: string }) {
  const created = await remindersApi.createReminder({
    patientId: patientId,
    title: input.title,
    dueAt: input.dueAt,
  });
  reminders.value.unshift(created);
}

async function onMarkDone(reminderId: string) {
  const updated = await remindersApi.markReminderDone(reminderId);
  reminders.value = reminders.value.map((r) =>
    r.id === reminderId ? updated : r
  );
}

async function onDismiss(reminderId: string) {
  const updated = await remindersApi.dismissReminder(reminderId);
  reminders.value = reminders.value.map((r) =>
    r.id === reminderId ? updated : r
  );
}

onMounted(() => {
  clockTimer = window.setInterval(() => {
    now.value = new Date();
  }, 1000); // tick every second
});

onBeforeUnmount(() => {
  if (clockTimer != null) {
    window.clearInterval(clockTimer);
  }
});

function startRecordDose(med: Medication) {
  recordingDoseForMedId.value = med.id;
  doseAmount.value = med.dosage ?? "";
  doseNotes.value = "";
}

const isLoading = ref(true);
const activeTab = ref<"visits" | "medications" | "reminders">("visits");

onMounted(() => {
  loadData();
});

async function loadData() {
  isLoading.value = true;
  try {
    const p = await patientsApi.get(patientId);
    patient.value = p;

    // owner
    if (p.ownerId) {
      try {
        owner.value = await ownersApi.get(p.ownerId);
      } catch {
        owner.value = null;
      }
    }

    // visits + visit details
    const baseVisits = await visitsApi.listForPatient(patientId);
    const details = await Promise.all(
      baseVisits.map(async (v) => {
        try {
          return await visitsApi.getDetails(v.id);
        } catch {
          // fall back to a stub details object
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
    // newest first
    details.sort((a, b) => {
      return (
        new Date(b.visit.dateTime).getTime() -
        new Date(a.visit.dateTime).getTime()
      );
    });
    visits.value = details;

    // medications for patient
    const meds = await patientsApi.listMedications(patientId);
    medications.value = meds;

    // dose events for each medication
    const doseMap: Record<string, DoseEvent[]> = {};

    for (const med of meds) {
      try {
        const events = await medicationsApi.listDoseEvents(med.id);
        doseMap[med.id] = events;
      } catch (err) {
        console.error("Failed to load dose events for medication", med.id, err);
        doseMap[med.id] = [];
      }
    }

    doseEventsByMedId.value = doseMap;
  } finally {
    isLoading.value = false;
  }
}

// --- Helpers ---

const ageLabel = computed(() => {
  const dob = patient.value?.dob;
  if (!dob) return null;
  const birth = new Date(dob);
  if (isNaN(birth.getTime())) return null;

  const now = new Date();
  let years = now.getFullYear() - birth.getFullYear();
  let months = now.getMonth() - birth.getMonth();
  if (months < 0) {
    years -= 1;
    months += 12;
  }
  if (years <= 0) return `${months} mo old`;
  if (months === 0) return `${years} years old`;
  return `${years} years ${months} mo old`;
});

function formatVisitDate(s: string): string {
  const d = new Date(s);
  if (isNaN(d.getTime())) return s;
  return d.toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

function isMedicationCompleted(m: Medication): boolean {
  if (!m.endDate) {
    return false;
  }

  const end = new Date(m.endDate);
  const today = new Date();

  end.setHours(23, 59, 59, 999);

  return end.getTime() < today.getTime();
}

function isMedicationActive(m: Medication): boolean {
  return !isMedicationCompleted(m);
}

function getNextDoseMeta(m: Medication) {
  const nextDueAt = (m as any).nextDueAt as string | undefined | null;
  if (!nextDueAt) return null;

  const target = new Date(nextDueAt);
  if (isNaN(target.getTime())) return null;

  const diffMs = target.getTime() - now.value.getTime();
  const isOverdue = diffMs <= 0;
  const absMs = Math.abs(diffMs);

  const totalSeconds = Math.floor(absMs / 1000);
  const days = Math.floor(totalSeconds / (60 * 60 * 24));
  const hours = Math.floor((totalSeconds % (60 * 60 * 24)) / (60 * 60));
  const minutes = Math.floor((totalSeconds % (60 * 60)) / 60);
  const seconds = totalSeconds % 60;

  let core: string;
  if (days > 0) {
    core = `${days}d ${hours}h`;
  } else if (hours > 0) {
    core = `${hours}h ${minutes}m`;
  } else if (minutes > 0) {
    core = `${minutes}m ${seconds}s`;
  } else {
    core = `${seconds}s`;
  }

  const label = isOverdue ? `overdue by ${core}` : `in ${core}`;

  // "soon" if it’s within the next hour
  const isSoon = !isOverdue && absMs <= 60 * 60 * 1000;

  return {
    label,
    isOverdue,
    isSoon,
  };
}

function getDoseProgress(m: Medication) {
  const last = (m as any).lastAdministeredAt as string | undefined | null;
  const next = (m as any).nextDueAt as string | undefined | null;

  if (!last || !next) return null;

  const lastDate = new Date(last);
  const nextDate = new Date(next);

  if (isNaN(lastDate.getTime()) || isNaN(nextDate.getTime())) return null;

  const totalMs = nextDate.getTime() - lastDate.getTime();
  if (totalMs <= 0) return null;

  const elapsedMs = now.value.getTime() - lastDate.getTime();
  const rawPercent = (elapsedMs / totalMs) * 100;
  const percent = Math.max(0, Math.min(100, rawPercent));

  const isOverdue = now.value.getTime() >= nextDate.getTime();

  return {
    percent,
    isOverdue,
    lastLabel: formatDoseDateTime(last),
    nextLabel: formatDoseDateTime(next),
  };
}

// color helpers (same idea as list view)
function speciesBadgeClass(species?: string | null): string {
  const s = (species ?? "").toLowerCase();

  if (s.includes("dog") || s.includes("canine"))
    return "bg-emerald-100 text-emerald-800";
  if (s.includes("cat") || s.includes("feline"))
    return "bg-sky-100 text-sky-800";
  if (s.includes("equine") || s.includes("horse"))
    return "bg-amber-100 text-amber-800";
  if (s.includes("bovine") || s.includes("cow"))
    return "bg-violet-100 text-violet-800";

  return "bg-slate-100 text-slate-800";
}

function sexBadgeClass(sex?: string | null): string {
  const s = (sex ?? "").toLowerCase();
  if (s === "f" || s.startsWith("hembra")) return "bg-pink-100 text-pink-800";
  if (s === "m" || s.startsWith("macho")) return "bg-blue-100 text-blue-800";
  return "bg-slate-100 text-slate-800";
}

// Actions (for now just TODOs)
function onNewVisit() {
  toast.info("New Visit", {
    description: "Visit creation flow not implemented yet.",
  });
}

function onAddMedication() {
  isMedicationDialogOpen.value = true;
}

function handleMedicationCreated(med: Medication) {
  medications.value = [med, ...medications.value];
}

function formatDoseDateTime(iso: string): string {
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  return d.toLocaleString(); // you can tweak to your preferred format
}

async function submitDose(med: Medication) {
  try {
    const created: DoseEvent = await medicationsApi.recordDose(med.id, {
      amount: doseAmount.value || undefined,
      notes: doseNotes.value || undefined,
    });

    // Append the new dose to the local list for that med
    doseEventsByMedId.value[med.id] = [
      created,
      ...(doseEventsByMedId.value[med.id] ?? []),
    ];

    medications.value = await patientsApi.listMedications(patientId);

    toast.success("Dose recorded");
    recordingDoseForMedId.value = null;
  } catch (err) {
    console.error(err);
    toast.error("Failed to record dose");
  }
}
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <Button variant="ghost" class="px-0 text-sm" @click="router.push('/')">
        ← Back to Patients
      </Button>

      <Card v-if="patient" class="border bg-background">
        <CardHeader
          class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between"
        >
          <div class="space-y-2">
            <CardTitle class="text-xl">
              {{ patient.name }}
            </CardTitle>

            <div
              class="flex flex-wrap items-center gap-2 text-sm text-muted-foreground"
            >
              <Badge
                :class="speciesBadgeClass(patient.species)"
                class="text-xs font-normal"
              >
                {{ patient.species || "Unknown species" }}
              </Badge>
              <span v-if="patient.breed"> • {{ patient.breed }} </span>
              <Badge
                v-if="patient.sex"
                :class="sexBadgeClass(patient.sex)"
                class="text-xs font-normal"
              >
                {{ patient.sex }}
              </Badge>
              <span v-if="ageLabel"> • {{ ageLabel }} </span>
            </div>
          </div>

          <div class="flex flex-wrap gap-2">
            <Button @click="onNewVisit"> 📅 New Visit </Button>
            <Button variant="outline" @click="onAddMedication">
              ➕ Add Medication
            </Button>
            <Button variant="outline" @click="onCreateReminder">
              🔔 Create Reminder
            </Button>
          </div>
        </CardHeader>

        <CardContent class="space-y-4">
          <Separator />

          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 text-sm">
            <!-- Owner -->
            <div class="space-y-1">
              <div class="text-xs uppercase text-muted-foreground">Owner</div>
              <div v-if="owner" class="space-y-1">
                <div
                  class="font-medium cursor-pointer hover:underline"
                  @click="router.push(`/owners/${patient.ownerId}`)"
                >
                  {{ owner.name }}
                </div>
                <div v-if="owner.phone" class="text-muted-foreground">
                  📞 {{ owner.phone }}
                </div>
                <div v-if="owner.email" class="text-muted-foreground">
                  ✉️ {{ owner.email }}
                </div>
              </div>
              <div v-else class="italic text-muted-foreground">
                Unknown owner
              </div>
            </div>

            <!-- Microchip -->
            <div class="space-y-1">
              <div class="text-xs uppercase text-muted-foreground">
                Microchip ID
              </div>
              <div class="font-medium">
                {{ patient.microchipId || "None" }}
              </div>
            </div>

            <!-- Allergies -->
            <div class="space-y-1">
              <div class="text-xs uppercase text-muted-foreground">
                Allergies
              </div>
              <div v-if="patient.allergies" class="flex flex-wrap gap-2">
                <Badge class="bg-red-100 text-red-800">
                  {{ patient.allergies }}
                </Badge>
              </div>
              <div v-else class="italic text-muted-foreground">
                No recorded allergies
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <div v-else-if="!isLoading" class="text-sm text-muted-foreground">
        Patient not found.
      </div>

      <!-- Tabs -->
      <Card v-if="patient">
        <CardContent class="pt-6">
          <Tabs v-model="activeTab" default-value="visits" class="w-full">
            <TabsList>
              <TabsTrigger value="visits">Visits</TabsTrigger>
              <TabsTrigger value="medications">Medications</TabsTrigger>
              <TabsTrigger value="reminders">Reminders</TabsTrigger>
            </TabsList>

            <!-- Visits tab -->
            <TabsContent value="visits" class="mt-4 space-y-3">
              <div
                v-if="visits.length === 0"
                class="text-sm text-muted-foreground italic py-4"
              >
                No previous visits for this patient.
              </div>

              <div
                v-for="vd in visits"
                :key="vd.visit.id"
                class="rounded-lg border bg-muted/50 px-4 py-3"
              >
                <div
                  class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between"
                >
                  <div class="font-medium">
                    {{ vd.visit.reason || "Visit" }}
                  </div>
                  <Badge class="text-xs font-normal">
                    {{ formatVisitDate(vd.visit.dateTime) }}
                  </Badge>
                </div>

                <div class="mt-2 space-y-2 text-sm text-muted-foreground">
                  <div v-if="vd.exams.length">
                    <span class="font-medium text-foreground text-xs uppercase">
                      Exams:
                    </span>
                    <div class="mt-1 flex flex-wrap gap-1">
                      <Badge
                        v-for="exam in vd.exams"
                        :key="exam.id"
                        variant="outline"
                        class="text-xs"
                      >
                        {{ exam.templateName }} • {{ exam.status }}
                      </Badge>
                    </div>
                  </div>

                  <div v-if="vd.medications.length">
                    <span class="font-medium text-foreground text-xs uppercase">
                      Medications:
                    </span>
                    <div class="mt-1 flex flex-wrap gap-1">
                      <Badge
                        v-for="m in vd.medications"
                        :key="m.id"
                        variant="outline"
                        class="text-xs"
                      >
                        {{ m.name }}
                      </Badge>
                    </div>
                  </div>

                  <div v-if="vd.attachments.length">
                    <span class="font-medium text-foreground text-xs uppercase">
                      Attachments:
                    </span>
                    <span class="ml-1">
                      {{ vd.attachments.length }} file(s)
                    </span>
                  </div>
                </div>
              </div>
            </TabsContent>

            <!-- Medications tab -->
            <TabsContent value="medications" class="mt-4 space-y-3">
              <div
                v-if="medications.length === 0"
                class="text-sm text-muted-foreground italic py-4"
              >
                No medications recorded.
              </div>

              <div
                v-for="med in medications"
                :key="med.id"
                class="rounded-lg border bg-muted/50 px-4 py-3 text-sm"
              >
                <div
                  class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between"
                >
                  <div class="font-medium">
                    {{ med.name }}
                  </div>
                  <Badge
                    :class="
                      isMedicationActive(med)
                        ? 'bg-emerald-100 text-emerald-800'
                        : 'bg-slate-100 text-slate-800'
                    "
                    class="text-xs font-normal"
                  >
                    {{ isMedicationActive(med) ? "Active" : "Completed" }}
                  </Badge>
                </div>

                <div class="mt-1 text-muted-foreground space-y-1">
                  <div v-if="med.dosage">Dosage: {{ med.dosage }}</div>
                  <div v-if="med.route">Route: {{ med.route }}</div>
                  <div v-if="med.frequency">Frequency: {{ med.frequency }}</div>

                  <div v-if="med.startDate || med.endDate" class="text-xs">
                    <span v-if="med.startDate">From {{ med.startDate }}</span>
                    <span v-if="med.startDate && med.endDate"> • </span>
                    <span v-if="med.endDate">To {{ med.endDate }}</span>
                  </div>

                  <!-- Active-only controls -->
                  <div
                    v-if="isMedicationActive(med)"
                    class="mt-2 flex flex-wrap items-center gap-2 text-xs"
                  >
                    <!-- 3.1 countdown pill (from previous step) -->
                    <div class="flex items-center gap-2">
                      <template v-if="getNextDoseMeta(med)">
                        <span
                          :class="[
                            'inline-flex items-center rounded-full px-2 py-0.5 font-medium',
                            getNextDoseMeta(med)?.isOverdue
                              ? 'bg-red-100 text-red-800 animate-pulse'
                              : getNextDoseMeta(med)?.isSoon
                              ? 'bg-amber-100 text-amber-800 animate-pulse'
                              : 'bg-emerald-100 text-emerald-800',
                          ]"
                        >
                          <span
                            class="mr-1 h-1.5 w-1.5 rounded-full"
                            :class="
                              getNextDoseMeta(med)?.isOverdue
                                ? 'bg-red-500'
                                : getNextDoseMeta(med)?.isSoon
                                ? 'bg-amber-500'
                                : 'bg-emerald-500'
                            "
                          />
                          next dose {{ getNextDoseMeta(med)?.label }}
                        </span>
                      </template>

                      <span v-else class="text-muted-foreground">
                        No next dose scheduled
                      </span>
                    </div>

                    <!-- 3.2 progress bar between lastAdministeredAt and nextDueAt -->
                    <div v-if="getDoseProgress(med)" class="space-y-1">
                      <div
                        class="flex justify-between text-[11px] text-muted-foreground"
                      >
                        <span
                          >Last dose:
                          {{ getDoseProgress(med)?.lastLabel }}</span
                        >
                        <span
                          >Next due: {{ getDoseProgress(med)?.nextLabel }}</span
                        >
                      </div>

                      <Progress
                        :model-value="getDoseProgress(med)?.percent ?? 0"
                        class="h-1.5"
                      />
                    </div>
                  </div>
                  <Button
                    size="sm"
                    variant="outline"
                    @click="startRecordDose(med)"
                  >
                    Record dose
                  </Button>
                </div>

                <div
                  v-if="recordingDoseForMedId === med.id"
                  class="mt-2 border-t pt-2 space-y-2 text-xs"
                >
                  <div
                    class="flex flex-col md:flex-row md:items-center md:gap-2"
                  >
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
                    <Button
                      size="sm"
                      variant="ghost"
                      @click="recordingDoseForMedId = null"
                    >
                      Cancel
                    </Button>
                    <Button size="sm" @click="submitDose(med)">
                      Save dose
                    </Button>
                  </div>
                </div>

                <div class="mt-2 border-t pt-2 text-xs text-muted-foreground">
                  <div class="font-medium mb-1">Dose history</div>

                  <div v-if="doseEventsByMedId[med.id]?.length">
                    <ul class="space-y-1 max-h-40 overflow-y-auto">
                      <li
                        v-for="dose in doseEventsByMedId[med.id]"
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
            </TabsContent>

            <!-- Reminders tab (placeholder) -->
            <TabsContent value="reminders" class="mt-4">
              <div class="text-sm text-muted-foreground italic py-4">
                Reminders are in progress and will appear here soon.
              </div>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
    <MedicationCreateDialog
      v-if="patient"
      v-model:open="isMedicationDialogOpen"
      :patient-id="patient.id"
      @created="handleMedicationCreated"
    />
  </MainLayout>
</template>

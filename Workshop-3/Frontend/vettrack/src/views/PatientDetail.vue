<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import MainLayout from "../components/layout/MainLayout.vue";

import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Separator } from "@/components/ui/separator";

import type {
  Patient,
  Owner,
  VisitDetails,
  Medication,
} from "../types/business";
import { patientsApi, ownersApi, visitsApi } from "../services/businessApi";
import { toast } from "vue-sonner";

const route = useRoute();
const router = useRouter();
const patientId = route.params.id as string;

const patient = ref<Patient | null>(null);
const owner = ref<Owner | null>(null);
const visits = ref<VisitDetails[]>([]);
const medications = ref<Medication[]>([]);

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
    medications.value = await patientsApi.listMedications(patientId);
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

function isMedicationActive(m: Medication): boolean {
  return m.isActive;
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
  toast.info("Add Medication", {
    description: "Medication creation flow not implemented yet.",
  });
}

function onCreateReminder() {
  toast.info("Reminders", { description: "Reminders are under construction." });
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
                    <span v-if="med.startDate"> From {{ med.startDate }} </span>
                    <span v-if="med.startDate && med.endDate"> • </span>
                    <span v-if="med.endDate"> To {{ med.endDate }} </span>
                  </div>
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
  </MainLayout>
</template>

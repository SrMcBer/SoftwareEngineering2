<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import MainLayout from "../components/layout/MainLayout.vue";
import PatientCreateDialog from "../components/patients/PatientCreateDialog.vue";
import PatientSearchCommand from "../components/patients/PatientSearchCommand.vue";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

import type { Patient, Owner, Visit } from "../types/business";
import { patientsApi, ownersApi, visitsApi } from "../services/businessApi";
import { useRouter } from "vue-router";

import { useTaxonomyStore } from "../stores/taxonomy";

const taxonomyStore = useTaxonomyStore();

function speciesBadgeClasses(species?: string | null): string {
  return taxonomyStore.speciesBadgeClasses(species);
}

function speciesEmoji(species?: string | null): string | undefined {
  return taxonomyStore.speciesEmoji(species);
}

function sexBadgeClasses(sex?: string | null): string {
  return taxonomyStore.sexBadgeClasses(sex);
}

function sexEmoji(sex?: string | null): string | undefined {
  return taxonomyStore.sexEmoji(sex);
}

const router = useRouter();

const patients = ref<Patient[]>([]);
const isLoading = ref(false);
const isCreateOpen = ref(false);
const isCommandOpen = ref(false);
const search = ref("");

// maps to avoid duplicate requests
const ownersById = ref<Record<string, Owner>>({});
const lastVisitsByPatientId = ref<Record<string, Visit | null>>({});

interface PatientRow extends Patient {
  ownerName?: string | null;
  ownerPhone?: string | null;
  ownerEmail?: string | null;
  lastVisitLabel?: string;
  lastVisitSortKey: number | null; // timestamp for sorting
}

const rows = ref<PatientRow[]>([]);
const sortMode = ref<"recent" | "needs">("recent");

async function loadPatients() {
  isLoading.value = true;
  try {
    const list = await patientsApi.list();
    patients.value = list;

    await Promise.all([hydrateOwners(list), hydrateLastVisits(list)]);
    buildRows();
  } finally {
    isLoading.value = false;
  }
}

// --- Owners: dedupe ownerIds then fetch each once ---
async function hydrateOwners(list: Patient[]) {
  const ownerIds = Array.from(
    new Set(list.map((p) => p.ownerId).filter((id): id is string => !!id))
  );

  const promises = ownerIds
    .filter((id) => !ownersById.value[id]) // skip already loaded
    .map(async (id) => {
      try {
        const owner = await ownersApi.get(id);
        ownersById.value[id] = owner;
      } catch {
        // if it fails, just leave it undefined
      }
    });

  await Promise.all(promises);
}

// --- Last visits: one call per patient (no dedupe possible) ---
async function hydrateLastVisits(list: Patient[]) {
  const promises = list.map(async (p) => {
    try {
      const visit = await visitsApi.getLastForPatient(p.id);
      lastVisitsByPatientId.value[p.id] = visit;
    } catch {
      lastVisitsByPatientId.value[p.id] = null;
    }
  });

  await Promise.all(promises);
}

// --- Build rows with owner + last visit info ---
function buildRows() {
  rows.value = patients.value.map((p) => {
    const owner = p.ownerId ? ownersById.value[p.ownerId] : undefined;
    const lastVisit = lastVisitsByPatientId.value[p.id] ?? null;

    let lastVisitLabel = "No visits yet";
    let lastVisitSortKey: number | null = null;

    if (lastVisit) {
      const date = new Date(lastVisit.dateTime);
      const formattedDate = date.toLocaleDateString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric",
      });

      const reason = lastVisit.reason || "Visit";
      lastVisitLabel = `${formattedDate} • ${reason}`;
      lastVisitSortKey = date.getTime();
    }

    return {
      ...p,
      ownerName: owner?.name ?? null,
      ownerPhone: owner?.phone ?? null,
      ownerEmail: owner?.email ?? null,
      lastVisitLabel,
      lastVisitSortKey,
    };
  });
}

onMounted(() => {
  loadPatients();
});

// --- Search + sort ---
// Sort first (by last visit recency), then filter.
const sortedRows = computed(() => {
  const withSort = [...rows.value];
  withSort.sort((a, b) => {
    const aKey = a.lastVisitSortKey;
    const bKey = b.lastVisitSortKey;

    if (sortMode.value === "recent") {
      if (aKey === null && bKey === null) return 0;
      if (aKey === null) return 1;
      if (bKey === null) return -1;
      return bKey - aKey; // newer date (bigger) first
    } else {
      // "Needs follow-up": older visits first, patients with NO visits at the top
      if (aKey === null && bKey === null) return 0;
      if (aKey === null) return -1; // never seen -> very overdue
      if (bKey === null) return 1;
      return aKey - bKey; // older date (smaller) first
    }
  });
  return withSort;
});

const filteredRows = computed(() => {
  const q = search.value.trim().toLowerCase();
  if (!q) return sortedRows.value;

  return sortedRows.value.filter((p) => {
    return (
      p.name.toLowerCase().includes(q) ||
      (p.species ?? "").toLowerCase().includes(q) ||
      (p.breed ?? "").toLowerCase().includes(q) ||
      (p.microchipId ?? "").toLowerCase().includes(q) ||
      (p.ownerName ?? "").toLowerCase().includes(q)
    );
  });
});

// --- Callbacks from dialogs/command ---
function handlePatientCreated(patient: Patient) {
  patients.value = [patient, ...patients.value];
  // fetch owner + last visit just for the new patient
  Promise.all([hydrateOwners([patient]), hydrateLastVisits([patient])]).then(
    buildRows
  );
}

function handleCommandSelect(patient: Patient) {
  router.push({ path: `/patients/${patient.id}` });
}

// ---- Color helpers ----
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <!-- Top row: search + add patient -->
      <div class="flex flex-col md:flex-row md:items-center gap-4">
        <div class="flex-1 max-w-xl flex items-center gap-2">
          <Input
            v-model="search"
            placeholder="Search by patient, owner, species, breed, or microchip"
            class="w-full"
          />
          <Button
            variant="outline"
            size="icon"
            class="shrink-0"
            @click="isCommandOpen = true"
            title="Advanced search (Ctrl/⌘ + K)"
          >
            ⌘K
          </Button>
        </div>

        <Button class="shrink-0 md:ml-auto" @click="isCreateOpen = true">
          + Add Patient
        </Button>
      </div>

      <!-- Table -->
      <Card>
        <CardHeader class="flex flex-row items-center justify-between gap-4">
          <CardTitle>Patients</CardTitle>

          <div class="inline-flex rounded-md border bg-muted/60 p-0.5 text-xs">
            <Button
              variant="ghost"
              size="sm"
              class="h-7 px-2"
              :class="
                sortMode === 'recent' ? 'bg-background shadow-sm' : 'opacity-70'
              "
              @click="sortMode = 'recent'"
            >
              Recently seen
            </Button>
            <Button
              variant="ghost"
              size="sm"
              class="h-7 px-2"
              :class="
                sortMode === 'needs' ? 'bg-background shadow-sm' : 'opacity-70'
              "
              @click="sortMode = 'needs'"
            >
              Needs follow-up
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div v-if="isLoading" class="py-10 text-center text-muted-foreground">
            Loading patients…
          </div>

          <div v-else class="border rounded-md bg-background overflow-hidden">
            <div
              class="grid grid-cols-[2fr,2fr,2.5fr,1.5fr,1fr] px-4 py-3 text-xs font-medium text-muted-foreground bg-muted/40"
            >
              <span>Patient</span>
              <span>Species / Breed</span>
              <span>Owner</span>
              <span>Last Visit</span>
              <span class="text-right">Actions</span>
            </div>

            <div
              v-if="filteredRows.length === 0"
              class="py-8 text-center text-sm text-muted-foreground"
            >
              No patients found.
            </div>

            <div v-else class="divide-y">
              <div
                v-for="patient in filteredRows"
                :key="patient.id"
                class="grid grid-cols-[2fr,2fr,2.5fr,1.5fr,1fr] px-4 py-3 items-center text-sm"
                @click="router.push(`/patients/${patient.id}`)"
              >
                <!-- Patient name + sex chip -->
                <div class="flex items-center gap-2">
                  <span class="font-medium">
                    {{ patient.name }}
                  </span>
                  <Badge
                    v-if="patient.sex"
                    :class="sexBadgeClasses(patient.sex)"
                    class="text-xs font-normal"
                  >
                    <span v-if="sexEmoji(patient.sex)" class="mr-1">
                      {{ sexEmoji(patient.sex) }}
                    </span>
                    {{ patient.sex }}
                  </Badge>
                </div>

                <!-- Species / Breed with colored chip -->
                <div class="flex items-center gap-2 text-muted-foreground">
                  <Badge
                    :class="speciesBadgeClasses(patient.species)"
                    class="text-xs font-normal"
                  >
                    <span v-if="speciesEmoji(patient.species)" class="mr-1">
                      {{ speciesEmoji(patient.species) }}
                    </span>
                    {{ patient.species || "Unknown" }}
                  </Badge>
                  <span v-if="patient.breed"> • {{ patient.breed }} </span>
                </div>

                <!-- Owner -->
                <div class="text-sm">
                  <div v-if="patient.ownerName" class="flex flex-col">
                    <span class="font-medium">
                      {{ patient.ownerName }}
                    </span>
                    <span
                      class="text-xs text-muted-foreground"
                      v-if="patient.ownerPhone || patient.ownerEmail"
                    >
                      <span v-if="patient.ownerPhone">
                        {{ patient.ownerPhone }}
                      </span>
                      <span v-if="patient.ownerPhone && patient.ownerEmail">
                        •
                      </span>
                      <span v-if="patient.ownerEmail">
                        {{ patient.ownerEmail }}
                      </span>
                    </span>
                  </div>
                  <span v-else class="text-xs text-muted-foreground italic">
                    Owner unknown
                  </span>
                </div>

                <!-- Last Visit -->
                <div class="text-sm text-muted-foreground">
                  <span v-if="patient.lastVisitSortKey !== null">
                    {{ patient.lastVisitLabel }}
                  </span>
                  <span v-else class="italic text-xs"> No visits yet </span>
                </div>

                <!-- Actions -->
                <div class="text-right">
                  <Button variant="link" class="px-0 text-sm">
                    View patient
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Simple stats -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardHeader>
            <CardTitle class="text-sm font-medium">Total Patients</CardTitle>
          </CardHeader>
          <CardContent class="text-2xl font-semibold">
            {{ patients.length }}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle class="text-sm font-medium">Visits This Week</CardTitle>
          </CardHeader>
          <CardContent class="text-2xl font-semibold"> 0 </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle class="text-sm font-medium"
              >Upcoming Reminders</CardTitle
            >
          </CardHeader>
          <CardContent class="text-2xl font-semibold"> 0 </CardContent>
        </Card>
      </div>
    </div>

    <!-- Modals -->
    <PatientCreateDialog
      v-model:open="isCreateOpen"
      @created="handlePatientCreated"
    />

    <PatientSearchCommand
      v-model:open="isCommandOpen"
      :patients="patients"
      @select="handleCommandSelect"
    />
  </MainLayout>
</template>

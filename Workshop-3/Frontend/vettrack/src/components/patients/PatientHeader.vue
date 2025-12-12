<script setup lang="ts">
import { useRouter } from "vue-router";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import type { Patient, Owner } from "../../types/business";
import { useFormatting } from "../../composables/useFormatting";

const props = defineProps<{
  patient: Patient;
  owner: Owner | null;
  ageLabel: string | null;
}>();

const emit = defineEmits<{
  newVisit: [];
  addMedication: [];
  createReminder: [];
}>();

const router = useRouter();
const { speciesBadgeClass, sexBadgeClass } = useFormatting();
</script>

<template>
  <Card class="border bg-background">
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
        <Button @click="emit('newVisit')"> 📅 New Visit </Button>
        <Button variant="outline" @click="emit('addMedication')">
          ➕ Add Medication
        </Button>
        <Button variant="outline" @click="emit('createReminder')">
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
          <div v-else class="italic text-muted-foreground">Unknown owner</div>
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
          <div class="text-xs uppercase text-muted-foreground">Allergies</div>
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
</template>

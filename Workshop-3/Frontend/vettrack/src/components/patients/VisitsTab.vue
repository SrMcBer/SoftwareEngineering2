<script setup lang="ts">
import { Badge } from "@/components/ui/badge";
import type { VisitDetails } from "../../types/business";
import { useFormatting } from "../../composables/useFormatting";

defineProps<{
  visits: VisitDetails[];
}>();

const { formatVisitDate } = useFormatting();
</script>

<template>
  <div class="mt-4 space-y-3">
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
          <span class="ml-1"> {{ vd.attachments.length }} file(s) </span>
        </div>
      </div>
    </div>
  </div>
</template>

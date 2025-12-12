<script setup lang="ts">
import { Badge } from "@/components/ui/badge";
import type { VisitDetails } from "../../types/business";
import { useFormatting } from "../../composables/useFormatting";
import { useRouter } from "vue-router";

defineProps<{
  visits: VisitDetails[];
}>();

const router = useRouter();
const { formatVisitDate } = useFormatting();

function goToVisit(visitId: string) {
  router.push(`/visits/${visitId}`);
}
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
      class="rounded-lg border bg-muted/50 px-4 py-3 hover:bg-muted/70 transition cursor-pointer"
      role="button"
      tabindex="0"
      @click="goToVisit(vd.visit.id)"
      @keydown.enter.prevent="goToVisit(vd.visit.id)"
      @keydown.space.prevent="goToVisit(vd.visit.id)"
    >
      <div
        class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between"
      >
        <div class="font-medium">
          {{ vd.visit.reason || "Visit" }}
        </div>

        <div class="flex items-center gap-2">
          <Badge class="text-xs font-normal">
            {{ formatVisitDate(vd.visit.dateTime) }}
          </Badge>

          <Button
            variant="outline"
            size="sm"
            class="h-7 px-2 text-xs"
            @click.stop="goToVisit(vd.visit.id)"
          >
            Open
          </Button>
        </div>
      </div>

      <!-- NEW: Notes + vitals -->
      <div class="mt-2 space-y-2 text-sm text-muted-foreground">
        <div v-if="vd.visit.examNotes" class="line-clamp-2">
          <span class="font-medium text-foreground text-xs uppercase">
            Notes:
          </span>
          <span class="ml-1">{{ vd.visit.examNotes }}</span>
        </div>

        <div v-if="vd.visit.vitalsJson">
          <span class="font-medium text-foreground text-xs uppercase">
            Vitals:
          </span>
          <span class="ml-1">{{ vd.visit.vitalsJson }}</span>
        </div>

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

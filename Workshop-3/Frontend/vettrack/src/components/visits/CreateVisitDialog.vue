<script setup lang="ts">
import { computed, reactive, watch } from "vue";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import type { Patient, Visit } from "@/types/business";
import { visitsApi } from "../../services/businessApi";
import { toast } from "vue-sonner";

const props = defineProps<{
  open: boolean;
  patient: Patient;
}>();

const emit = defineEmits<{
  "update:open": [value: boolean];
  created: [visit: Visit];
}>();

const state = reactive({
  saving: false,
  reason: "",
  examNotes: "",
  weightKg: "" as string | number,
  heartRate: "" as string | number,
  temperatureC: "" as string | number,
  respiratoryRate: "" as string | number,
});

const canSubmit = computed(
  () => state.reason.trim().length > 0 && !state.saving
);

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      // reset each time it opens
      state.saving = false;
      state.reason = "";
      state.examNotes = "";
      state.weightKg = "";
      state.heartRate = "";
      state.temperatureC = "";
      state.respiratoryRate = "";
    }
  }
);

function close() {
  if (state.saving) return;
  emit("update:open", false);
}

async function submit() {
  if (!canSubmit.value) return;

  try {
    state.saving = true;

    const toNumberOrUndefined = (v: string | number) => {
      if (v === "" || v === null || v === undefined) return undefined;
      const n = Number(v);
      return Number.isFinite(n) ? n : undefined;
    };

    const visit = await visitsApi.create({
      patientId: props.patient.id,
      reason: state.reason.trim(),
      examNotes: state.examNotes.trim() || undefined,
      weightKg: toNumberOrUndefined(state.weightKg),
      heartRate: toNumberOrUndefined(state.heartRate),
      temperatureC: toNumberOrUndefined(state.temperatureC),
      respiratoryRate: toNumberOrUndefined(state.respiratoryRate),
    });

    toast.success("Visit created", {
      description: "You can now add exams from templates.",
    });
    emit("created", visit);
    emit("update:open", false);
  } catch (e: any) {
    toast.error("Could not create visit", {
      description: e?.message ?? "Unknown error",
    });
  } finally {
    state.saving = false;
  }
}
</script>

<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent
      class="sm:max-w-[560px]"
      @interact-outside.prevent
      @escape-key-down.prevent
    >
      <DialogHeader>
        <DialogTitle>New Visit</DialogTitle>
      </DialogHeader>

      <div class="space-y-4">
        <div class="space-y-2">
          <Label for="reason">Reason *</Label>
          <Input
            id="reason"
            v-model="state.reason"
            placeholder="e.g., Vaccination, Limping, Checkup..."
          />
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="weight">Weight (kg)</Label>
            <Input id="weight" v-model="state.weightKg" inputmode="decimal" />
          </div>
          <div class="space-y-2">
            <Label for="hr">Heart rate</Label>
            <Input id="hr" v-model="state.heartRate" inputmode="numeric" />
          </div>
          <div class="space-y-2">
            <Label for="temp">Temp (°C)</Label>
            <Input id="temp" v-model="state.temperatureC" inputmode="decimal" />
          </div>
          <div class="space-y-2">
            <Label for="rr">Resp. rate</Label>
            <Input
              id="rr"
              v-model="state.respiratoryRate"
              inputmode="numeric"
            />
          </div>
        </div>

        <div class="space-y-2">
          <Label for="notes">Notes</Label>
          <Textarea
            id="notes"
            v-model="state.examNotes"
            rows="4"
            placeholder="Optional visit notes..."
          />
        </div>
      </div>

      <DialogFooter class="gap-2">
        <Button variant="outline" :disabled="state.saving" @click="close">
          Cancel
        </Button>
        <Button :disabled="!canSubmit" @click="submit">
          {{ state.saving ? "Creating..." : "Create visit" }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

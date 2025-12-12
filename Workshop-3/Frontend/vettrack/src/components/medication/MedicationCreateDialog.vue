<script setup lang="ts">
import { ref, watch } from "vue";
import { toast } from "vue-sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";

// 🆕 Select components
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import type {
  Medication,
  PrescribeMedicationRequest,
} from "../../types/business";
import { medicationsApi } from "../../services/businessApi";

// 🔹 Standardised route & frequency options
const ROUTE_OPTIONS = [
  { value: "PO", label: "PO – Oral" },
  { value: "SC", label: "SC – Subcutaneous" },
  { value: "IM", label: "IM – Intramuscular" },
  { value: "IV", label: "IV – Intravenous" },
  { value: "TOP", label: "TOP – Topical" },
];

const FREQUENCY_OPTIONS = [
  { value: "SID", label: "SID – q24h" },
  { value: "BID", label: "BID – q12h" },
  { value: "TID", label: "TID – q8h" },
  { value: "QID", label: "QID – q6h" },
  { value: "EOD", label: "EOD – Every other day" },
];

const props = defineProps<{
  open: boolean;
  patientId: string;
}>();

const emit = defineEmits<{
  (e: "update:open", value: boolean): void;
  (e: "created", medication: Medication): void;
}>();

const internalOpen = ref(props.open);
watch(
  () => props.open,
  (v) => (internalOpen.value = v)
);
watch(internalOpen, (v) => emit("update:open", v));

const form = ref<PrescribeMedicationRequest>({
  patientId: props.patientId,
  name: "",
  dosage: "",
  route: "",
  frequency: "",
  startDate: "",
  endDate: "",
});

const isSubmitting = ref(false);
const errorMsg = ref<string | null>(null);

watch(
  () => props.patientId,
  (id) => {
    form.value.patientId = id;
  }
);

function resetForm() {
  form.value = {
    patientId: props.patientId,
    name: "",
    dosage: "",
    route: "",
    frequency: "",
    startDate: "",
    endDate: "",
  };
  errorMsg.value = null;
}

async function handleSubmit() {
  errorMsg.value = null;

  if (!form.value.name.trim()) {
    errorMsg.value = "Medication name is required.";
    return;
  }

  isSubmitting.value = true;
  try {
    const payload: PrescribeMedicationRequest = {
      ...form.value,
      patientId: props.patientId,
    };
    const med = await medicationsApi.prescribe(payload);
    toast.success("Medication prescribed", {
      description: `${med.name} has been added.`,
    });
    emit("created", med);
    resetForm();
    internalOpen.value = false;
  } catch {
    errorMsg.value =
      "Failed to prescribe medication. Please check the data and try again.";
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <Dialog v-model:open="internalOpen">
    <DialogContent class="sm:max-w-lg">
      <DialogHeader>
        <DialogTitle>Add Medication</DialogTitle>
        <DialogDescription>
          Prescribe a new medication for this patient.
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 py-2">
        <div class="space-y-1">
          <Label for="med-name">Name</Label>
          <Input
            id="med-name"
            v-model="form.name"
            placeholder="e.g. Meloxicam"
          />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="space-y-1">
            <Label for="med-dosage">Dosage</Label>
            <Input
              id="med-dosage"
              v-model="form.dosage"
              placeholder="0.1 mg/kg"
            />
          </div>

          <div class="space-y-1">
            <Label for="med-route">Route</Label>
            <Select v-model="form.route">
              <SelectTrigger id="med-route">
                <SelectValue placeholder="Select route" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="option in ROUTE_OPTIONS"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div class="space-y-1">
          <Label for="med-frequency">Frequency</Label>
          <Select v-model="form.frequency">
            <SelectTrigger id="med-frequency">
              <SelectValue placeholder="Select frequency" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem
                v-for="option in FREQUENCY_OPTIONS"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="space-y-1">
            <Label for="med-start">Start date</Label>
            <Input id="med-start" type="date" v-model="form.startDate" />
          </div>

          <div class="space-y-1">
            <Label for="med-end">End date</Label>
            <Input id="med-end" type="date" v-model="form.endDate" />
          </div>
        </div>

        <p v-if="errorMsg" class="text-sm text-red-500">
          {{ errorMsg }}
        </p>
      </div>

      <DialogFooter>
        <Button
          variant="outline"
          @click="internalOpen = false"
          :disabled="isSubmitting"
        >
          Cancel
        </Button>
        <Button @click="handleSubmit" :disabled="isSubmitting">
          <span v-if="isSubmitting">Saving…</span>
          <span v-else>Save</span>
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

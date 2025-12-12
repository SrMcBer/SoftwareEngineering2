<script setup lang="ts">
import { computed, reactive, watch } from "vue";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import type { Patient, UpdatePatientRequest } from "@/types/business";
import { patientsApi } from "../../services/businessApi";

const props = defineProps<{
  open: boolean;
  patient: Patient;
}>();

const emit = defineEmits<{
  "update:open": [boolean];
  updated: [Patient];
}>();

const form = reactive<UpdatePatientRequest>({
  name: "",
  species: "",
  breed: "",
  sex: "",
  dob: "",
  color: "",
  microchipId: "",
  allergies: "",
  notes: "",
});

watch(
  () => props.patient,
  (p) => {
    form.name = p.name ?? "";
    form.species = p.species ?? "";
    form.breed = p.breed ?? "";
    form.sex = p.sex ?? "";
    form.dob = p.dob ?? "";
    form.color = p.color ?? "";
    form.microchipId = p.microchipId ?? "";
    form.allergies = p.allergies ?? "";
    form.notes = p.notes ?? "";
  },
  { immediate: true }
);

function diffPayload(): UpdatePatientRequest {
  const p = props.patient;
  const payload: UpdatePatientRequest = {};
  (
    [
      "name",
      "species",
      "breed",
      "sex",
      "dob",
      "color",
      "microchipId",
      "allergies",
      "notes",
    ] as const
  ).forEach((k) => {
    const next = (form[k] ?? "").trim();
    const prev = ((p as any)[k] ?? "").toString().trim();
    if (next !== prev) (payload as any)[k] = next === "" ? null : next; // use null only if your backend supports clearing
  });
  return payload;
}

const canSave = computed(() => Object.keys(diffPayload()).length > 0);

async function onSave() {
  const payload = diffPayload();
  const updated = await patientsApi.update(props.patient.id, payload);
  emit("updated", updated);
  emit("update:open", false);
}
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="sm:max-w-[640px]">
      <DialogHeader>
        <DialogTitle>Edit patient</DialogTitle>
      </DialogHeader>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="space-y-2">
          <label class="text-sm text-muted-foreground">Name</label>
          <Input v-model="form.name" />
        </div>

        <div class="space-y-2">
          <label class="text-sm text-muted-foreground">Species</label>
          <Input v-model="form.species" />
        </div>

        <div class="space-y-2">
          <label class="text-sm text-muted-foreground">Breed</label>
          <Input v-model="form.breed" />
        </div>

        <div class="space-y-2">
          <label class="text-sm text-muted-foreground">Sex</label>
          <Input v-model="form.sex" />
        </div>

        <div class="space-y-2">
          <label class="text-sm text-muted-foreground">DOB</label>
          <Input v-model="form.dob" placeholder="YYYY-MM-DD" />
        </div>

        <div class="space-y-2">
          <label class="text-sm text-muted-foreground">Microchip ID</label>
          <Input v-model="form.microchipId" />
        </div>

        <div class="space-y-2 md:col-span-2">
          <label class="text-sm text-muted-foreground">Allergies</label>
          <Input v-model="form.allergies" />
        </div>

        <div class="space-y-2 md:col-span-2">
          <label class="text-sm text-muted-foreground">Notes</label>
          <Textarea v-model="form.notes" rows="4" />
        </div>
      </div>

      <div class="flex justify-end gap-2 pt-2">
        <Button variant="outline" @click="emit('update:open', false)"
          >Cancel</Button
        >
        <Button :disabled="!canSave" @click="onSave">Save</Button>
      </div>
    </DialogContent>
  </Dialog>
</template>

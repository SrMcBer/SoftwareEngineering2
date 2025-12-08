<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import {
  Command,
  CommandInput,
  CommandList,
  CommandEmpty,
  CommandGroup,
  CommandItem,
} from "@/components/ui/command";
import type { Patient } from "../../types/business";

const props = defineProps<{
  open: boolean;
  patients: Patient[];
}>();

const emit = defineEmits<{
  (e: "update:open", value: boolean): void;
  (e: "select", patient: Patient): void;
}>();

const internalOpen = ref(props.open);
const query = ref("");

onMounted(() => {
  window.addEventListener("keydown", handleKeydown);
});

onUnmounted(() => {
  window.removeEventListener("keydown", handleKeydown);
});

function handleKeydown(e: KeyboardEvent) {
  if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") {
    e.preventDefault();
    internalOpen.value = true;
    emit("update:open", true);
  }
}

function close() {
  internalOpen.value = false;
  emit("update:open", false);
}

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return props.patients;
  return props.patients.filter((p) => {
    return (
      p.name.toLowerCase().includes(q) ||
      (p.species ?? "").toLowerCase().includes(q) ||
      (p.breed ?? "").toLowerCase().includes(q) ||
      (p.microchipId ?? "").toLowerCase().includes(q)
    );
  });
});

function selectPatient(patient: Patient) {
  emit("select", patient);
  close();
}
</script>

<template>
  <Dialog
    v-model:open="internalOpen"
    @update:open="emit('update:open', $event)"
  >
    <DialogContent class="p-0 gap-0 max-w-lg">
      <Command>
        <CommandInput v-model="query" placeholder="Search patients…" />
        <CommandList>
          <CommandEmpty>No patients found.</CommandEmpty>
          <CommandGroup heading="Patients">
            <CommandItem
              v-for="patient in filtered"
              :key="patient.id"
              :value="patient.name"
              @select="() => selectPatient(patient)"
            >
              <div class="flex flex-col">
                <span class="font-medium">
                  {{ patient.name }}
                </span>
                <span class="text-xs text-muted-foreground">
                  {{ patient.species }}
                  <span v-if="patient.breed"> • {{ patient.breed }}</span>
                  <span v-if="patient.microchipId">
                    • Chip: {{ patient.microchipId }}
                  </span>
                </span>
              </div>
            </CommandItem>
          </CommandGroup>
        </CommandList>
      </Command>
    </DialogContent>
  </Dialog>
</template>

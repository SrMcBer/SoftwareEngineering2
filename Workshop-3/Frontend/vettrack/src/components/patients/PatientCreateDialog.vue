<script setup lang="ts">
import { ref, watch, computed, onMounted } from "vue";
import { toast } from "vue-sonner";
import { useTaxonomyStore } from "../../stores/taxonomy";

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
import {
  Popover,
  PopoverTrigger,
  PopoverContent,
} from "@/components/ui/popover";
import {
  Command,
  CommandInput,
  CommandList,
  CommandEmpty,
  CommandGroup,
  CommandItem,
} from "@/components/ui/command";
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";

import type {
  Patient,
  RegisterPatientRequest,
  Owner,
  CreateOwnerRequest,
} from "../../types/business";
import { patientsApi, ownersApi } from "../../services/businessApi";

const props = defineProps<{
  open: boolean;
  ownerId?: string; // pre-selected owner (optional)
}>();

const emit = defineEmits<{
  (e: "update:open", value: boolean): void;
  (e: "created", patient: Patient): void;
}>();

// dialog open
const internalOpen = ref(props.open);
watch(
  () => props.open,
  (v) => (internalOpen.value = v)
);
watch(internalOpen, (v) => emit("update:open", v));

// owners state
const owners = ref<Owner[]>([]);
const ownersLoading = ref(false);
const ownerPopoverOpen = ref(false);
const ownerSearch = ref("");
const selectedOwnerId = ref<string | null>(props.ownerId ?? null);

// inline owner creation state
const creatingOwner = ref(false);
const newOwner = ref<CreateOwnerRequest>({
  name: "",
  phone: "",
  email: "",
});

// patient form
const form = ref<RegisterPatientRequest>({
  ownerId: props.ownerId ?? "",
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
const taxonomyStore = useTaxonomyStore();

const speciesMode = ref<"preset" | "custom">("preset");
const sexMode = ref<"preset" | "custom">("preset");

const presetSpecies = computed(() => taxonomyStore.speciesOptions);
const presetSex = computed(() => taxonomyStore.sexOptions);

const isSubmitting = ref(false);
const errorMsg = ref<string | null>(null);

const selectedOwner = computed(
  () => owners.value.find((o) => o.id === selectedOwnerId.value) ?? null
);

watch(
  () => selectedOwnerId.value,
  (id) => {
    form.value.ownerId = id ?? "";
  }
);

async function ensureOwnersLoaded() {
  if (owners.value.length > 0 || ownersLoading.value) return;
  ownersLoading.value = true;
  try {
    const list = await ownersApi.list(); // no filter, we filter client side
    owners.value = list;
  } finally {
    ownersLoading.value = false;
  }
}

watch(
  () => internalOpen.value,
  (open) => {
    if (open) {
      ensureOwnersLoaded();
    }
  }
);

onMounted(() => {
  if (internalOpen.value) {
    ensureOwnersLoaded();
  }
});

function resetForm() {
  form.value = {
    ownerId: props.ownerId ?? "",
    name: "",
    species: "",
    breed: "",
    sex: "",
    dob: "",
    color: "",
    microchipId: "",
    allergies: "",
    notes: "",
  };
  selectedOwnerId.value = props.ownerId ?? null;
  creatingOwner.value = false;
  newOwner.value = {
    name: "",
    phone: "",
    email: "",
  };
  errorMsg.value = null;
}

async function handleSubmit() {
  errorMsg.value = null;

  // owner handling
  if (!selectedOwnerId.value && !creatingOwner.value) {
    errorMsg.value = "Please select an owner or create a new one.";
    return;
  }

  let ownerIdToUse = selectedOwnerId.value;

  if (!ownerIdToUse && creatingOwner.value) {
    if (!newOwner.value.name.trim()) {
      errorMsg.value = "Owner name is required.";
      return;
    }
    try {
      const created = await ownersApi.create(newOwner.value);
      owners.value.unshift(created);
      ownerIdToUse = created.id;
      selectedOwnerId.value = created.id;
    } catch {
      errorMsg.value = "Failed to create owner.";
      return;
    }
  }

  if (!ownerIdToUse) {
    errorMsg.value = "Owner is required.";
    return;
  }

  if (!form.value.name.trim() || !form.value.species.trim()) {
    errorMsg.value = "Patient name and species are required.";
    return;
  }

  if (form.value.species) {
    const item = taxonomyStore.ensureSpecies(form.value.species);
    form.value.species = item.label; // or item.value if you prefer
  }

  if (form.value.sex) {
    const item = taxonomyStore.ensureSex(form.value.sex);
    form.value.sex = item.label;
  }

  isSubmitting.value = true;
  try {
    const payload: RegisterPatientRequest = {
      ...form.value,
      ownerId: ownerIdToUse,
    };
    const patient = await patientsApi.register(payload);
    toast.success("Patient created", {
      description: `${patient.name} has been added.`,
    });
    emit("created", patient);
    resetForm();
    internalOpen.value = false;
  } catch {
    errorMsg.value =
      "Failed to create patient. Please check the data and try again.";
  } finally {
    isSubmitting.value = false;
  }
}

function startCreatingOwnerFromSearch() {
  creatingOwner.value = true;
  newOwner.value.name = ownerSearch.value.trim();
}

const filteredOwners = computed(() => {
  const q = ownerSearch.value.trim().toLowerCase();
  if (!q) return owners.value;
  return owners.value.filter((o) => o.name.toLowerCase().includes(q));
});
</script>

<template>
  <Dialog v-model:open="internalOpen">
    <DialogContent class="sm:max-w-lg">
      <DialogHeader>
        <DialogTitle>Add Patient</DialogTitle>
        <DialogDescription>
          Register a new patient in VetTrack. Select an existing owner or create
          a new one.
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 py-2">
        <!-- Owner picker -->
        <div class="space-y-1">
          <Label>Owner</Label>

          <Popover v-model:open="ownerPopoverOpen">
            <PopoverTrigger as-child>
              <Button
                variant="outline"
                role="combobox"
                class="w-full justify-between"
                @click="ensureOwnersLoaded"
              >
                <span v-if="selectedOwner">
                  {{ selectedOwner.name }}
                  <span
                    v-if="selectedOwner.email"
                    class="text-xs text-muted-foreground"
                  >
                    • {{ selectedOwner.email }}
                  </span>
                </span>
                <span v-else class="text-muted-foreground">
                  Select owner…
                </span>
                <span class="ml-2 text-xs text-muted-foreground"> ▼ </span>
              </Button>
            </PopoverTrigger>

            <PopoverContent class="w-[--radix-popover-trigger-width] p-0">
              <Command>
                <CommandInput
                  v-model="ownerSearch"
                  placeholder="Search owners by name…"
                />
                <CommandList>
                  <CommandEmpty>
                    <div class="py-2 text-center text-sm text-muted-foreground">
                      No owners found.
                    </div>
                  </CommandEmpty>

                  <CommandGroup v-if="filteredOwners.length">
                    <CommandItem
                      v-for="ownerItem in filteredOwners"
                      :key="ownerItem.id"
                      :value="ownerItem.name"
                      @select="
                        () => {
                          selectedOwnerId = ownerItem.id;
                          creatingOwner = false;
                          ownerPopoverOpen = false;
                        }
                      "
                    >
                      <div class="flex flex-col">
                        <span>{{ ownerItem.name }}</span>
                        <span
                          class="text-xs text-muted-foreground"
                          v-if="ownerItem.email || ownerItem.phone"
                        >
                          <span v-if="ownerItem.phone">{{
                            ownerItem.phone
                          }}</span>
                          <span v-if="ownerItem.phone && ownerItem.email">
                            •
                          </span>
                          <span v-if="ownerItem.email">{{
                            ownerItem.email
                          }}</span>
                        </span>
                      </div>
                    </CommandItem>
                  </CommandGroup>

                  <CommandGroup>
                    <CommandItem
                      value="__create_owner__"
                      class="text-emerald-700"
                      @select="
                        () => {
                          startCreatingOwnerFromSearch();
                          ownerPopoverOpen = false;
                        }
                      "
                    >
                      + Create owner
                      <span v-if="ownerSearch" class="ml-1 font-medium">
                        “{{ ownerSearch }}”
                      </span>
                    </CommandItem>
                  </CommandGroup>
                </CommandList>
              </Command>
            </PopoverContent>
          </Popover>

          <!-- Inline new owner form -->
          <div
            v-if="creatingOwner"
            class="mt-3 space-y-2 rounded-md border bg-muted/40 p-3"
          >
            <div class="text-xs font-medium text-muted-foreground uppercase">
              New owner details
            </div>
            <div class="space-y-1">
              <Label for="new-owner-name">Name</Label>
              <Input
                id="new-owner-name"
                v-model="newOwner.name"
                placeholder="Owner name"
              />
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div class="space-y-1">
                <Label for="new-owner-phone">Phone</Label>
                <Input
                  id="new-owner-phone"
                  v-model="newOwner.phone"
                  placeholder="(555) 123-4567"
                />
              </div>
              <div class="space-y-1">
                <Label for="new-owner-email">Email</Label>
                <Input
                  id="new-owner-email"
                  type="email"
                  v-model="newOwner.email"
                  placeholder="owner@email.com"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- Patient core fields -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="space-y-1">
            <Label for="name">Name</Label>
            <Input id="name" v-model="form.name" placeholder="Bella" />
          </div>

          <div class="space-y-1">
            <Label>Species</Label>

            <Select
              v-if="speciesMode === 'preset'"
              @update:modelValue="
                (val) => {
                  if (val && typeof val === 'string') form.species = val;
                }
              "
            >
              <SelectTrigger>
                <SelectValue placeholder="Select a species" />
              </SelectTrigger>

              <SelectContent>
                <SelectItem
                  v-for="s in presetSpecies"
                  :key="s.id"
                  :value="s.label"
                >
                  <span v-if="s.emoji" class="mr-1">{{ s.emoji }}</span>
                  {{ s.label }}
                </SelectItem>

                <SelectItem value="__custom__" @click="speciesMode = 'custom'">
                  + Custom species…
                </SelectItem>
              </SelectContent>
            </Select>

            <div v-else class="flex items-center gap-2">
              <Input
                v-model="form.species"
                placeholder="Enter custom species"
              />
              <Button
                variant="outline"
                size="sm"
                @click="
                  speciesMode = 'preset';
                  form.species = '';
                "
              >
                Back
              </Button>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="space-y-1">
            <Label for="breed">Breed</Label>
            <Input
              id="breed"
              v-model="form.breed"
              placeholder="Golden Retriever"
            />
          </div>

          <div class="space-y-1">
            <Label>Sex</Label>

            <Select
              v-if="sexMode === 'preset'"
              @update:modelValue="
                (val) => {
                  if (val && typeof val === 'string') form.sex = val;
                }
              "
            >
              <SelectTrigger>
                <SelectValue placeholder="Select sex" />
              </SelectTrigger>

              <SelectContent>
                <SelectItem v-for="s in presetSex" :key="s.id" :value="s.label">
                  <span v-if="s.emoji" class="mr-1">{{ s.emoji }}</span>
                  {{ s.label }}
                </SelectItem>

                <SelectItem value="__custom__" @click="sexMode = 'custom'">
                  + Custom…
                </SelectItem>
              </SelectContent>
            </Select>

            <div v-else class="flex items-center gap-2">
              <Input v-model="form.sex" placeholder="Enter custom value" />
              <Button
                variant="outline"
                size="sm"
                @click="
                  sexMode = 'preset';
                  form.sex = '';
                "
              >
                Back
              </Button>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="space-y-1">
            <Label for="dob">Date of birth</Label>
            <Input id="dob" type="date" v-model="form.dob" />
          </div>

          <div class="space-y-1">
            <Label for="color">Color</Label>
            <Input id="color" v-model="form.color" placeholder="Brown" />
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="space-y-1">
            <Label for="microchip">Microchip ID</Label>
            <Input
              id="microchip"
              v-model="form.microchipId"
              placeholder="123456789"
            />
          </div>

          <div class="space-y-1">
            <Label for="allergies">Allergies</Label>
            <Input
              id="allergies"
              v-model="form.allergies"
              placeholder="e.g. penicillin"
            />
          </div>
        </div>

        <div class="space-y-1">
          <Label for="notes">Notes</Label>
          <Input
            id="notes"
            v-model="form.notes"
            placeholder="Behavior, chronic conditions, etc."
          />
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

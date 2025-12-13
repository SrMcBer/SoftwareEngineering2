<script setup lang="ts">
import { computed, reactive, watch, ref } from "vue";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

import type { Owner, UpdateOwnerRequest } from "@/types/business";
import { ownersApi } from "@/services/businessApi";

const props = defineProps<{
  open: boolean;
  owner: Owner;
}>();

const emit = defineEmits<{
  "update:open": [boolean];
  updated: [Owner];
}>();

const saving = ref(false);

const form = reactive<UpdateOwnerRequest>({
  name: "",
  phone: "",
  email: "",
});

watch(
  () => props.owner,
  (o) => {
    form.name = o.name ?? "";
    form.phone = o.phone ?? "";
    form.email = o.email ?? "";
  },
  { immediate: true }
);

function diff(): UpdateOwnerRequest {
  const o = props.owner;
  const payload: UpdateOwnerRequest = {};
  if ((form.name ?? "").trim() !== (o.name ?? "").trim())
    payload.name = (form.name ?? "").trim();
  if ((form.phone ?? "").trim() !== (o.phone ?? "").trim())
    payload.phone = (form.phone ?? "").trim();
  if ((form.email ?? "").trim() !== (o.email ?? "").trim())
    payload.email = (form.email ?? "").trim();
  return payload;
}

const canSave = computed(() => Object.keys(diff()).length > 0);

async function onSave() {
  saving.value = true;
  try {
    const updated = await ownersApi.update(props.owner.id, diff());
    emit("updated", updated);
    emit("update:open", false);
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="sm:max-w-[520px]">
      <DialogHeader>
        <DialogTitle>Edit owner</DialogTitle>
      </DialogHeader>

      <div class="grid gap-4">
        <div class="space-y-2">
          <label class="text-sm text-muted-foreground">Name</label>
          <Input v-model="form.name" />
        </div>

        <div class="space-y-2">
          <label class="text-sm text-muted-foreground">Phone</label>
          <Input v-model="form.phone" />
        </div>

        <div class="space-y-2">
          <label class="text-sm text-muted-foreground">Email</label>
          <Input v-model="form.email" />
        </div>
      </div>

      <div class="flex justify-end gap-2 pt-4">
        <Button
          variant="outline"
          :disabled="saving"
          @click="emit('update:open', false)"
          >Cancel</Button
        >
        <Button :disabled="!canSave || saving" @click="onSave">Save</Button>
      </div>
    </DialogContent>
  </Dialog>
</template>

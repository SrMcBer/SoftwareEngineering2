<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import MainLayout from "../components/layout/MainLayout.vue";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";

import type { Owner, Patient } from "@/types/business";
import { ownersApi, patientsApi } from "../services/businessApi";
import EditOwnerDialog from "@/components/owners/EditOwnerDialog.vue";

const route = useRoute();
const router = useRouter();

const owner = ref<Owner | null>(null);
const pets = ref<Patient[]>([]);
const loading = ref(false);
const editOpen = ref(false);

async function load() {
  const id = String(route.params.id);
  loading.value = true;
  try {
    const [o, p] = await Promise.all([
      ownersApi.get(id),
      patientsApi.listByOwner(id),
    ]);
    owner.value = o;
    pets.value = p;
  } finally {
    loading.value = false;
  }
}

function handleOwnerUpdated(updated: Owner) {
  owner.value = updated;
}

onMounted(load);
watch(() => route.params.id, load);
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <Card class="border bg-background">
        <CardHeader
          class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between"
        >
          <div class="space-y-1">
            <CardTitle class="text-xl">
              {{ owner?.name ?? "Owner" }}
            </CardTitle>

            <div
              class="flex flex-wrap items-center gap-2 text-sm text-muted-foreground"
            >
              <span v-if="owner?.phone">📞 {{ owner.phone }}</span>
              <span v-if="owner?.email">• ✉️ {{ owner.email }}</span>
            </div>
          </div>

          <div class="flex gap-2">
            <Button variant="outline" @click="router.back()">← Back</Button>
            <Button @click="editOpen = true">✏️ Edit Owner</Button>
          </div>
        </CardHeader>

        <CardContent class="space-y-4">
          <Separator />

          <div class="flex items-center justify-between">
            <div class="text-sm font-medium">Pets</div>
            <Badge variant="secondary">{{ pets.length }}</Badge>
          </div>

          <div v-if="loading" class="text-sm text-muted-foreground">
            Loading…
          </div>

          <div
            v-else-if="pets.length === 0"
            class="text-sm text-muted-foreground italic"
          >
            No pets registered for this owner.
          </div>

          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <Card
              v-for="p in pets"
              :key="p.id"
              class="cursor-pointer hover:shadow-sm transition"
              @click="router.push(`/patients/${p.id}`)"
            >
              <CardHeader class="py-4">
                <div class="flex items-center justify-between gap-3">
                  <div class="font-medium">{{ p.name }}</div>
                  <Badge variant="secondary" class="text-xs">{{
                    p.species
                  }}</Badge>
                </div>
                <div class="text-sm text-muted-foreground">
                  <span v-if="p.breed">{{ p.breed }}</span>
                  <span v-if="p.sex"> • {{ p.sex }}</span>
                </div>
              </CardHeader>
            </Card>
          </div>
        </CardContent>
      </Card>

      <EditOwnerDialog
        v-if="owner"
        v-model:open="editOpen"
        :owner="owner"
        @updated="handleOwnerUpdated"
      />
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-vue-next";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import MainLayout from "../components/layout/MainLayout.vue";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { toast } from "vue-sonner";
import { useFormatting } from "@/composables/useFormatting";

import type {
  VisitDetails,
  ExamTemplate,
  VisitExamSummary,
  Exam,
  Attachment,
} from "@/types/business";

import {
  visitsApi,
  examTemplatesApi,
  examsApi,
  attachmentsApi,
} from "../services/businessApi";

const route = useRoute();
const router = useRouter();
const { formatVisitDate } = useFormatting();

const visitId = computed(() => String(route.params.visitId));

const loading = ref(true);
const details = ref<VisitDetails | null>(null);

const addExamOpen = ref(false);
const templatesLoading = ref(false);
const templates = ref<ExamTemplate[]>([]);
const selectedTemplateId = ref<string | null>(null);
const creatingExam = ref(false);

const addAttachmentOpen = ref(false);
const uploadingAttachment = ref(false);
const attachmentType = ref<string>("image");
const attachmentFile = ref<File | null>(null);

function openAttachment(a: any) {
  const url = a.downloadUrl || a.url;
  if (url) {
    window.open(url, "_blank", "noopener,noreferrer");
  }
}

async function uploadAttachment() {
  if (!details.value || !attachmentFile.value) return;
  try {
    uploadingAttachment.value = true;
    await attachmentsApi.upload({
      patientId: details.value.patient.id,
      visitId: details.value.visit.id,
      type: attachmentType.value,
      file: attachmentFile.value,
    });
    toast.success("Attachment uploaded");
    addAttachmentOpen.value = false;
    attachmentFile.value = null;
    await load(); // refresh visit details
  } catch (e: any) {
    toast.error("Could not upload attachment", {
      description: e?.message ?? "Unknown error",
    });
  } finally {
    uploadingAttachment.value = false;
  }
}

function goBack() {
  if (window.history.length > 1) router.back();
  else if (details.value?.patient?.id)
    router.push(`/patients/${details.value.patient.id}`);
  else router.push("/");
}

async function load() {
  loading.value = true;
  try {
    details.value = await visitsApi.getDetails(visitId.value);
  } catch (e: any) {
    toast.error("Could not load visit", {
      description: e?.message ?? "Unknown error",
    });
  } finally {
    loading.value = false;
  }
}

onMounted(load);

const title = computed(() => details.value?.visit.reason || "Visit");
const subtitle = computed(() =>
  details.value ? formatVisitDate(details.value.visit.dateTime) : ""
);

async function openAddExam() {
  addExamOpen.value = true;
  selectedTemplateId.value = null;

  if (templates.value.length) return;

  templatesLoading.value = true;
  try {
    templates.value = await examTemplatesApi.listActive();
  } catch (e: any) {
    toast.error("Could not load templates", {
      description: e?.message ?? "Unknown error",
    });
  } finally {
    templatesLoading.value = false;
  }
}

function goToExam(examId: string) {
  // if you already have this route, great. Otherwise comment out for now.
  router.push({ name: "exam-details", params: { examId } });
}

async function createExam() {
  if (!details.value || !selectedTemplateId.value) return;

  try {
    creatingExam.value = true;

    const res: Exam = await examsApi.createFromTemplate({
      patientId: details.value.patient.id,
      visitId: details.value.visit.id,
      templateId: selectedTemplateId.value,
      resultsJson: "{}",
    });

    toast.success("Exam created", { description: "Saved as draft." });
    addExamOpen.value = false;

    // refresh visit details so the exam appears
    await load();

    // optionally jump straight to exam page
    // goToExam(res.id);
  } catch (e: any) {
    toast.error("Could not create exam", {
      description: e?.message ?? "Unknown error",
    });
  } finally {
    creatingExam.value = false;
  }
}

async function finalize(exam: VisitExamSummary) {
  if (exam.status === "final") return;

  try {
    await examsApi.finalize(exam.id);
    toast.success("Exam finalized");
    await load();
  } catch (e: any) {
    toast.error("Could not finalize exam", {
      description: e?.message ?? "Unknown error",
    });
  }
}
</script>

<template>
  <MainLayout>
    <div class="space-y-4">
      <Card>
        <CardHeader class="flex flex-col gap-2">
          <div class="flex items-start justify-between gap-3">
            <div>
              <CardTitle class="text-xl">{{ title }}</CardTitle>
              <div class="mt-1 text-sm text-muted-foreground">
                {{ subtitle }}
                <span v-if="details"> • {{ details.patient.name }}</span>
              </div>
            </div>

            <Button variant="outline" size="sm" @click="goBack">
              <ArrowLeft class="h-4 w-4 mr-2" />
              Back
            </Button>

            <Button :disabled="loading" @click="openAddExam">
              + Add exam
            </Button>
          </div>
        </CardHeader>

        <CardContent v-if="loading" class="text-sm text-muted-foreground">
          Loading visit...
        </CardContent>

        <CardContent v-else-if="!details" class="text-sm text-muted-foreground">
          Visit not found.
        </CardContent>

        <CardContent v-else class="space-y-4">
          <div class="text-sm">
            <div v-if="details.visit.examNotes" class="space-y-1">
              <div class="text-xs uppercase text-muted-foreground">Notes</div>
              <div class="whitespace-pre-wrap">
                {{ details.visit.examNotes }}
              </div>
            </div>
          </div>

          <div v-if="details.visit.vitalsJson" class="space-y-1">
            <div class="text-xs uppercase text-muted-foreground">Vitals</div>
            <div class="text-sm text-muted-foreground">
              {{ details.visit.vitalsJson }}
            </div>
          </div>

          <Separator />

          <div class="space-y-2">
            <div class="flex items-center justify-between">
              <div class="text-xs uppercase text-muted-foreground">Exams</div>
              <Badge variant="outline">{{ details.exams.length }}</Badge>
            </div>

            <div
              v-if="details.exams.length === 0"
              class="text-sm text-muted-foreground italic"
            >
              No exams yet. Add one from a template.
            </div>

            <div v-else class="space-y-2">
              <div
                v-for="ex in details.exams"
                :key="ex.id"
                class="rounded-lg border bg-muted/40 px-4 py-3 flex items-center justify-between gap-3"
              >
                <div class="min-w-0">
                  <div class="font-medium truncate">{{ ex.templateName }}</div>
                  <div class="text-xs text-muted-foreground">
                    Status: <span class="font-medium">{{ ex.status }}</span>
                  </div>
                </div>

                <div class="flex items-center gap-2">
                  <Button variant="outline" size="sm" @click="goToExam(ex.id)">
                    View / edit
                  </Button>
                  <Button
                    v-if="ex.status !== 'final'"
                    size="sm"
                    @click="finalize(ex)"
                  >
                    Finalize
                  </Button>
                </div>
              </div>
            </div>
          </div>

          <Separator />

          <div class="space-y-2">
            <div class="text-xs uppercase text-muted-foreground">
              Medications
            </div>
            <div
              v-if="details.medications.length === 0"
              class="text-sm text-muted-foreground italic"
            >
              No medications recorded for this visit.
            </div>
            <div v-else class="flex flex-wrap gap-2">
              <Badge
                v-for="m in details.medications"
                :key="m.id"
                variant="outline"
              >
                {{ m.name }}
              </Badge>
            </div>
          </div>

          <Separator />

          <div class="space-y-2">
            <div class="flex items-center justify-between">
              <div class="text-xs uppercase text-muted-foreground">
                Attachments
              </div>

              <div class="flex items-center gap-2">
                <Badge variant="outline">{{
                  details.attachments.length
                }}</Badge>
                <Button size="sm" @click="addAttachmentOpen = true"
                  >+ Add attachment</Button
                >
              </div>
            </div>

            <div
              v-if="details.attachments.length === 0"
              class="text-sm text-muted-foreground italic"
            >
              No attachments yet.
            </div>

            <div v-else class="space-y-2">
              <div
                v-for="a in details.attachments"
                :key="a.id"
                class="rounded-lg border bg-muted/40 px-4 py-3 flex items-center justify-between gap-3"
              >
                <div class="min-w-0">
                  <div class="font-medium truncate">
                    {{ a.filename || "Attachment" }}
                  </div>
                  <div class="text-xs text-muted-foreground flex gap-2">
                    <Badge variant="outline">{{ a.type }}</Badge>
                    <span>Uploaded: {{ a.createdAt }}</span>
                  </div>
                </div>

                <div class="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    @click="openAttachment(a)"
                  >
                    Open
                  </Button>
                  <!-- optional -->
                  <!-- <Button variant="destructive" size="sm" @click="deleteAttachment(a.id)">Delete</Button> -->
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Add Exam Dialog -->
      <Dialog :open="addExamOpen" @update:open="(v) => (addExamOpen = v)">
        <DialogContent class="sm:max-w-[520px]">
          <DialogHeader>
            <DialogTitle>Add exam from template</DialogTitle>
          </DialogHeader>

          <div class="space-y-2">
            <div v-if="templatesLoading" class="text-sm text-muted-foreground">
              Loading templates...
            </div>

            <div
              v-else-if="templates.length === 0"
              class="text-sm text-muted-foreground italic"
            >
              No active templates available.
            </div>

            <div v-else class="space-y-2">
              <button
                v-for="t in templates"
                :key="t.id"
                type="button"
                class="w-full text-left rounded-lg border px-4 py-3 hover:bg-muted"
                :class="selectedTemplateId === t.id ? 'border-primary' : ''"
                @click="selectedTemplateId = t.id"
              >
                <div class="font-medium">{{ t.name }}</div>
                <div v-if="t.description" class="text-xs text-muted-foreground">
                  {{ t.description }}
                </div>
              </button>
            </div>
          </div>

          <DialogFooter class="gap-2">
            <Button
              variant="outline"
              :disabled="creatingExam"
              @click="addExamOpen = false"
            >
              Cancel
            </Button>
            <Button
              :disabled="creatingExam || !selectedTemplateId"
              @click="createExam"
            >
              {{ creatingExam ? "Creating..." : "Create exam" }}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <!-- Add Attachment Dialog -->
      <Dialog
        :open="addAttachmentOpen"
        @update:open="(v) => (addAttachmentOpen = v)"
      >
        <DialogContent class="sm:max-w-[520px]">
          <DialogHeader>
            <DialogTitle>Add attachment</DialogTitle>
          </DialogHeader>

          <div class="space-y-3">
            <div class="space-y-1">
              <div class="text-xs uppercase text-muted-foreground">Type</div>
              <select
                v-model="attachmentType"
                class="w-full rounded-md border bg-background px-3 py-2 text-sm"
              >
                <option value="image">image</option>
                <option value="pdf">pdf</option>
                <option value="video">video</option>
                <option value="other">other</option>
              </select>
            </div>

            <div class="space-y-1">
              <div class="text-xs uppercase text-muted-foreground">File</div>
              <input
                type="file"
                class="w-full text-sm"
                @change="(e:any) => (attachmentFile = e.target.files?.[0] ?? null)"
              />
            </div>
          </div>

          <DialogFooter class="gap-2">
            <Button
              variant="outline"
              :disabled="uploadingAttachment"
              @click="addAttachmentOpen = false"
            >
              Cancel
            </Button>
            <Button
              :disabled="uploadingAttachment || !attachmentFile"
              @click="uploadAttachment"
            >
              {{ uploadingAttachment ? "Uploading..." : "Upload" }}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  </MainLayout>
</template>

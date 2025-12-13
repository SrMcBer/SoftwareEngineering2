// src/services/businessApi.ts
import { coreHttp } from "./httpClient";
import type {
  Owner,
  CreateOwnerRequest,
  UpdateOwnerRequest,
  Patient,
  RegisterPatientRequest,
  UpdatePatientRequest,
  Visit,
  CreateVisitRequest,
  UpdateVisitRequest,
  VisitDetails,
  Medication,
  PrescribeMedicationRequest,
  UpdateMedicationRequest,
  EndMedicationRequest,
  RecordDoseRequest,
  DoseEventResponse,
  DoseEvent,
  Exam,
  ExamTemplate,
  CreateExamTemplateRequest,
  CreateExamFromTemplateRequest,
  HealthResponse,
  Reminder,
  CreateReminderPayload,
  Attachment,
  UploadAttachmentParams,
} from "../types/business";

// ---------- Owners ----------

export interface ListOwnersParams {
  name?: string; // contains, case-insensitive
}

export const ownersApi = {
  async list(params?: ListOwnersParams): Promise<Owner[]> {
    const { data } = await coreHttp.get<Owner[]>("/owners", { params });
    return data;
  },

  async get(id: string): Promise<Owner> {
    const { data } = await coreHttp.get<Owner>(`/owners/${id}`);
    return data;
  },

  async create(payload: CreateOwnerRequest): Promise<Owner> {
    const { data } = await coreHttp.post<Owner>("/owners", payload);
    return data;
  },

  async update(id: string, payload: UpdateOwnerRequest): Promise<Owner> {
    const { data } = await coreHttp.put<Owner>(`/owners/${id}`, payload);
    return data;
  },

  async delete(id: string): Promise<void> {
    await coreHttp.delete(`/owners/${id}`);
  },
};

// ---------- Patients ----------

export const patientsApi = {
  async list(): Promise<Patient[]> {
    const { data } = await coreHttp.get<Patient[]>("/patients");
    return data;
  },

  async listByOwner(ownerId: string): Promise<Patient[]> {
    const { data } = await coreHttp.get<Patient[]>(`/patients`, {
      params: { ownerId },
    });
    return data;
  },

  async get(id: string): Promise<Patient> {
    const { data } = await coreHttp.get<Patient>(`/patients/${id}`);
    return data;
  },

  async register(payload: RegisterPatientRequest): Promise<Patient> {
    const { data } = await coreHttp.post<Patient>("/patients", payload);
    return data;
  },

  async update(id: string, payload: UpdatePatientRequest): Promise<Patient> {
    const { data } = await coreHttp.put<Patient>(`/patients/${id}`, payload);
    return data;
  },

  async findByMicrochip(microchipId: string): Promise<Patient> {
    const { data } = await coreHttp.get<Patient>(
      `/patients/by-microchip/${microchipId}`
    );
    return data;
  },

  async listMedications(patientId: string): Promise<Medication[]> {
    const { data } = await coreHttp.get<Medication[]>(
      `/patients/${patientId}/medications`
    );
    return data;
  },
};

// ---------- Visits ----------

export interface ListVisitsBetweenParams {
  start: string; // ISO date-time
  end: string; // ISO date-time
}

export const visitsApi = {
  async listAll(): Promise<Visit[]> {
    const { data } = await coreHttp.get<Visit[]>("/visits");
    return data;
  },

  async get(id: string): Promise<Visit> {
    const { data } = await coreHttp.get<Visit>(`/visits/${id}`);
    return data;
  },

  async getDetails(id: string): Promise<VisitDetails> {
    const { data } = await coreHttp.get<VisitDetails>(`/visits/${id}/details`);
    return data;
  },

  async create(payload: CreateVisitRequest): Promise<Visit> {
    const { data } = await coreHttp.post<Visit>("/visits", payload);
    return data;
  },

  async update(id: string, payload: UpdateVisitRequest): Promise<Visit> {
    const { data } = await coreHttp.put<Visit>(`/visits/${id}`, payload);
    return data;
  },

  async listForPatient(patientId: string): Promise<Visit[]> {
    const { data } = await coreHttp.get<Visit[]>(
      `/visits/patient/${patientId}`
    );
    return data;
  },

  async getLastForPatient(patientId: string): Promise<Visit | null> {
    const { data } = await coreHttp.get<Visit | null>(
      `/visits/patient/${patientId}/last`
    );
    return data;
  },

  async searchBetween(params: ListVisitsBetweenParams): Promise<Visit[]> {
    const { data } = await coreHttp.get<Visit[]>("/visits/search", {
      params,
    });
    return data;
  },
};

// ---------- Medications ----------

export const medicationsApi = {
  async get(medicationId: string): Promise<Medication> {
    const { data } = await coreHttp.get<Medication>(
      `/medications/${medicationId}`
    );
    return data;
  },

  async prescribe(payload: PrescribeMedicationRequest): Promise<Medication> {
    const { data } = await coreHttp.post<Medication>("/medications", payload);
    return data;
  },

  async update(
    medicationId: string,
    payload: UpdateMedicationRequest
  ): Promise<Medication> {
    const { data } = await coreHttp.put<Medication>(
      `/medications/${medicationId}`,
      payload
    );
    return data;
  },

  async end(
    medicationId: string,
    payload: EndMedicationRequest
  ): Promise<Medication> {
    const { data } = await coreHttp.post<Medication>(
      `/medications/${medicationId}/end`,
      payload
    );
    return data;
  },

  async recordDose(
    medicationId: string,
    payload: RecordDoseRequest
  ): Promise<DoseEvent> {
    const { data } = await coreHttp.post<DoseEvent>(
      `/medications/${medicationId}/doses`,
      payload
    );
    return data;
  },

  async listDoseEvents(medicationId: string) {
    const res = await coreHttp.get(`/medications/${medicationId}/doses`);
    return res.data as DoseEventResponse[];
  },
};

// ---------- Exams & templates ----------

export const examsApi = {
  async get(examId: string): Promise<Exam> {
    const { data } = await coreHttp.get<Exam>(`/exams/${examId}`);
    return data;
  },

  async createFromTemplate(
    payload: CreateExamFromTemplateRequest
  ): Promise<Exam> {
    const { data } = await coreHttp.post<Exam>("/exams/from-template", payload);
    return data;
  },

  async finalize(examId: string): Promise<Exam> {
    const { data } = await coreHttp.post<Exam>(`/exams/${examId}/finalize`);
    return data;
  },

  async listForPatient(patientId: string): Promise<Exam[]> {
    const { data } = await coreHttp.get<Exam[]>(`/exams/patient/${patientId}`);
    return data;
  },
};

export const remindersApi = {
  // --- Reminders ---

  /**
   * Create / schedule a new reminder for a patient.
   * POST /reminders
   */
  async createReminder(payload: CreateReminderPayload): Promise<Reminder> {
    const { data } = await coreHttp.post<Reminder>("/reminders", payload);
    return data;
  },

  /**
   * Get all reminders for a specific patient.
   * GET /reminders/patient/{patientId}
   */
  async getRemindersForPatient(patientId: string): Promise<Reminder[]> {
    const { data } = await coreHttp.get<Reminder[]>(
      `/reminders/patient/${patientId}`
    );
    return data;
  },

  /**
   * Mark a reminder as done.
   * PATCH /reminders/{id}/done
   */
  async markReminderDone(reminderId: string): Promise<Reminder> {
    const { data } = await coreHttp.patch<Reminder>(
      `/reminders/${reminderId}/done`
    );
    return data;
  },

  /**
   * Dismiss a reminder.
   * PATCH /reminders/{id}/dismiss
   */
  async dismissReminder(reminderId: string): Promise<Reminder> {
    const { data } = await coreHttp.patch<Reminder>(
      `/reminders/${reminderId}/dismiss`
    );
    return data;
  },

  /**
   * List all overdue reminders (optional but useful for a global view).
   * GET /reminders/overdue
   */
  async listOverdueReminders(): Promise<Reminder[]> {
    const { data } = await coreHttp.get<Reminder[]>("/reminders/overdue");
    return data;
  },
};

export const examTemplatesApi = {
  async create(payload: CreateExamTemplateRequest): Promise<ExamTemplate> {
    const { data } = await coreHttp.post<ExamTemplate>(
      "/exam-templates",
      payload
    );
    return data;
  },

  async listAll(): Promise<ExamTemplate[]> {
    const { data } = await coreHttp.get<ExamTemplate[]>("/exam-templates");
    return data;
  },

  async get(templateId: string): Promise<ExamTemplate> {
    const { data } = await coreHttp.get<ExamTemplate>(
      `/exam-templates/${templateId}`
    );
    return data;
  },

  async listActive(): Promise<ExamTemplate[]> {
    const { data } = await coreHttp.get<ExamTemplate[]>(
      "/exam-templates/active"
    );
    return data;
  },
};

export const attachmentsApi = {
  async upload(params: UploadAttachmentParams): Promise<Attachment> {
    // enforce XOR client-side too (DB enforces it as well) :contentReference[oaicite:8]{index=8}
    if (!!params.visitId && !!params.examId) {
      throw new Error(
        "Attachment can be linked to a visit OR an exam, not both."
      );
    }

    const fd = new FormData();
    fd.append("file", params.file);

    const qs = new URLSearchParams();
    qs.set("type", params.type);
    if (params.visitId) qs.set("visitId", params.visitId);
    if (params.examId) qs.set("examId", params.examId);

    const { data } = await coreHttp.post<Attachment>(
      `/api/v1/patients/${params.patientId}/attachments?${qs.toString()}`,
      {
        method: "POST",
        body: fd,
        // NOTE: don't set Content-Type manually for FormData
      }
    );
    return data;
  },

  async remove(attachmentId: string): Promise<void> {
    await coreHttp.delete(`/api/v1/attachments/${attachmentId}`);
  },
};

// ---------- Health ----------

export const healthApi = {
  async check(): Promise<HealthResponse> {
    const { data } = await coreHttp.get<HealthResponse>("/health");
    return data;
  },
};

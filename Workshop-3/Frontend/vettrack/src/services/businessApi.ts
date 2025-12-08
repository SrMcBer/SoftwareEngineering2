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
  DoseEvent,
  Exam,
  ExamTemplate,
  CreateExamTemplateRequest,
  CreateExamFromTemplateRequest,
  HealthResponse,
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

// ---------- Health ----------

export const healthApi = {
  async check(): Promise<HealthResponse> {
    const { data } = await coreHttp.get<HealthResponse>("/health");
    return data;
  },
};

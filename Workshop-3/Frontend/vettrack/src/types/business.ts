// src/types/business.ts

// ---------- Owners ----------

export interface Owner {
  id: string;           // uuid
  name: string;
  phone?: string | null;
  email?: string | null;
  createdAt?: string;   // date-time
  updatedAt?: string;   // date-time
}

export interface CreateOwnerRequest {
  name: string;
  phone?: string;
  email?: string;
}

export interface UpdateOwnerRequest {
  name?: string;
  phone?: string;
  email?: string;
}

// ---------- Patients ----------

export interface Patient {
  id: string;          // uuid
  ownerId: string;     // uuid
  name: string;
  species: string;
  breed?: string | null;
  sex?: string | null;
  dob?: string | null;          // date (ISO string)
  color?: string | null;
  microchipId?: string | null;
  allergies?: string | null;
  notes?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface RegisterPatientRequest {
  ownerId: string;
  name: string;
  species: string;
  breed?: string;
  sex?: string;
  dob?: string;               // date
  color?: string;
  microchipId?: string;
  allergies?: string;
  notes?: string;
}

export interface UpdatePatientRequest {
  name?: string;
  species?: string;
  breed?: string;
  sex?: string;
  dob?: string;              // date
  color?: string;
  microchipId?: string;
  allergies?: string;
  notes?: string;
}

// ---------- Visits ----------

export interface Visit {
  id: string;                    // uuid
  patientId: string;             // uuid
  dateTime: string;              // date-time
  reason: string;
  vitalsJson?: string | null;
  examNotes?: string | null;
  diagnoses?: string | null;
  procedures?: string | null;
  recommendations?: string | null;
  createdByUserId?: string | null; // uuid
  createdAt: string;             // date-time
  updatedAt: string;             // date-time
}

export interface CreateVisitRequest {
  patientId: string;
  reason: string;
  examNotes?: string;
  weightKg?: number;
  heartRate?: number;
  temperatureC?: number;
  respiratoryRate?: number;
  diagnoses?: string;
  procedures?: string;
  recommendations?: string;
}

export interface UpdateVisitRequest {
  reason?: string;
  examNotes?: string;
  weightKg?: number;
  heartRate?: number;
  temperatureC?: number;
  respiratoryRate?: number;
  diagnoses?: string;
  procedures?: string;
  recommendations?: string;
}

// Summaries used in VisitDetails

export interface VisitPatientSummary {
  id: string;             // uuid
  ownerId?: string;       // uuid
  name: string;
  species: string;
  breed?: string | null;
  sex?: string | null;
  dob?: string | null;    // date
  color?: string | null;
}

export interface VisitExamSummary {
  id: string;                 // uuid
  templateId: string;         // uuid
  templateName: string;
  status: string;
  performedAt: string;        // date-time
  performedByUserId?: string; // uuid
  vitalsJson?: string | null;
  resultsJson?: string | null;
}

export interface VisitMedicationSummary {
  id: string;             // uuid
  name: string;
  dosage?: string | null;
  route?: string | null;
  frequency?: string | null;
  startDate?: string | null;          // date
  endDate?: string | null;            // date
  lastAdministeredAt?: string | null; // date-time
  nextDueAt?: string | null;          // date-time
}

export interface VisitAttachmentSummary {
  id: string;        // uuid
  type?: string;
  filename?: string;
  url?: string;
  createdAt: string; // date-time
}

export interface VisitDetails {
  visit: Visit;
  patient: VisitPatientSummary;
  exams: VisitExamSummary[];
  medications: VisitMedicationSummary[];
  attachments: VisitAttachmentSummary[];
}

// ---------- Medications ----------

export interface Medication {
  id: string;            // uuid
  patientId: string;     // uuid
  name: string;
  dosage?: string | null;
  route?: string | null;
  frequency?: string | null;
  startDate?: string | null;   // date
  endDate?: string | null;     // date
  isActive: boolean;
  createdByUserId?: string | null; // uuid
  createdAt: string;         // date-time
  updatedAt: string;         // date-time
}

export interface PrescribeMedicationRequest {
  patientId: string;
  name: string;
  dosage?: string;
  route?: string;
  frequency?: string;
  startDate?: string;    // date
  endDate?: string;      // date
}

export interface UpdateMedicationRequest {
  name?: string;
  dosage?: string;
  route?: string;
  frequency?: string;
  startDate?: string;    // date
  endDate?: string;      // date
}

export interface EndMedicationRequest {
  endDate?: string;      // date
}

export interface RecordDoseRequest {
  amount?: string;
  notes?: string;
}

export interface DoseEvent {
  id: string;            // uuid
  medicationId: string;  // uuid
  occurredAt: string;    // date-time
  amount?: string | null;
  notes?: string | null;
  recordedByUserId?: string | null; // uuid
}

// ---------- Exams & templates ----------

export interface ExamTemplate {
  id: string;           // uuid
  name: string;
  description?: string | null;
  fieldsJson: string;
  isActive: boolean;
  version: number;
  createdByUserId?: string | null; // uuid
  createdAt: string;               // date-time
  updatedAt: string;               // date-time
}

export interface CreateExamTemplateRequest {
  name: string;
  description?: string;
  fieldsJson: string;
  version?: number;
}

export type ExamStatus = string; // "DRAFT" | "FINAL" etc, keep free-form for now

export interface Exam {
  id: string;            // uuid
  patientId: string;     // uuid
  visitId: string;       // uuid
  templateId: string;    // uuid
  status: ExamStatus;
  vitalsJson?: string | null;
  resultsJson?: string | null;
  createdByUserId?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateExamFromTemplateRequest {
  patientId: string;
  visitId: string;
  templateId: string;
  vitalsJson?: string;
  resultsJson: string;
  status?: ExamStatus;
}

// ---------- Health ----------

export interface HealthResponse {
  status: string;
  db: string;
  dbDetails?: string;
}

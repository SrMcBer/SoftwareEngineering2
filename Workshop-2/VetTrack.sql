-- =====================================================================
-- VetTrack — PostgreSQL DDL
-- =====================================================================

-- UUID generator
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================
-- Enums
-- =========================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
    CREATE TYPE user_role AS ENUM ('vet', 'admin');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'reminder_status') THEN
    CREATE TYPE reminder_status AS ENUM ('pending', 'done', 'overdue');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'exam_status') THEN
    CREATE TYPE exam_status AS ENUM ('draft', 'final');
  END IF;
END$$;

-- =========================
-- Users & Ownership
-- =========================
CREATE TABLE app_user (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name             TEXT NOT NULL,
  email            CITEXT UNIQUE NOT NULL,
  role             user_role NOT NULL DEFAULT 'vet',
  password_hash    TEXT NOT NULL,
  status           BOOLEAN NOT NULL DEFAULT TRUE,
  last_login_at    TIMESTAMPTZ,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE owner (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name             TEXT NOT NULL,
  phone            TEXT,
  email            CITEXT,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================
-- Patients & Clinical Data
-- =========================
CREATE TABLE patient (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  owner_id         UUID REFERENCES owner(id) ON DELETE SET NULL,
  name             TEXT NOT NULL,
  species          TEXT NOT NULL,          -- e.g., Canine, Feline
  breed            TEXT,
  sex              TEXT,                   -- simple text; normalize if needed
  dob              DATE,                   -- or approximate age stored in notes
  color            TEXT,
  microchip_id     TEXT,
  allergies        TEXT,
  notes            TEXT,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_patient_microchip UNIQUE (microchip_id)
);

-- Visits (consultations)
CREATE TABLE visit (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  patient_id       UUID NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
  date_time        TIMESTAMPTZ NOT NULL DEFAULT now(),
  reason           TEXT,
  vitals_json      JSONB,                  -- snapshot for this visit (temp, HR, RR, weight, etc.)
  exam_notes       TEXT,
  diagnoses        TEXT,
  procedures       TEXT,
  recommendations  TEXT,
  created_by       UUID REFERENCES app_user(id),
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Medications (current or past)
CREATE TABLE medication (
  id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  patient_id           UUID NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
  name                 TEXT NOT NULL,
  dosage               TEXT,                -- e.g., "25 mg"
  route                TEXT,                -- e.g., "PO", "IM"
  frequency            TEXT,                -- e.g., "BID", "q12h"
  start_date           DATE,
  end_date             DATE,
  last_administered_at TIMESTAMPTZ,
  next_due_at          TIMESTAMPTZ,
  created_by           UUID REFERENCES app_user(id),
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Optional per-dose history
CREATE TABLE dose_event (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  medication_id    UUID NOT NULL REFERENCES medication(id) ON DELETE CASCADE,
  administered_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  amount           TEXT,       -- if you want to log the exact amount
  notes            TEXT,
  recorded_by      UUID REFERENCES app_user(id)
);

-- Reminders / Follow-ups
CREATE TABLE reminder (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  patient_id       UUID NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
  title            TEXT NOT NULL,
  due_at           TIMESTAMPTZ NOT NULL,
  status           reminder_status NOT NULL DEFAULT 'pending',
  created_by       UUID REFERENCES app_user(id),
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================
-- Exams & Templates
-- =========================

-- A template defines the schema (fields) for exams; edited via version bump
CREATE TABLE exam_template (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name             TEXT NOT NULL,
  version          INTEGER NOT NULL DEFAULT 1,
  description      TEXT,
  is_active        BOOLEAN NOT NULL DEFAULT TRUE,
  fields_json      JSONB NOT NULL,  -- array of {key,label,type,...}
  created_by       UUID REFERENCES app_user(id),
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_exam_template_name_version UNIQUE (name, version)
);

-- An exam is an instance performed on a patient (optionally tied to a visit)
CREATE TABLE exam (
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  patient_id         UUID NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
  visit_id           UUID REFERENCES visit(id) ON DELETE SET NULL,
  template_id        UUID NOT NULL REFERENCES exam_template(id) ON DELETE RESTRICT,
  template_version   INTEGER NOT NULL,      -- snapshot from template.version at creation
  performed_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  performed_by       UUID REFERENCES app_user(id),
  vitals_json        JSONB,                  -- vitals captured for this exam
  results_json       JSONB NOT NULL,         -- values matching template fields
  status             varchar(20) DEFAULT 'draft',
  notes              TEXT,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================
-- Attachments (files)
-- =========================
CREATE TABLE attachment (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  patient_id       UUID NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
  visit_id         UUID REFERENCES visit(id) ON DELETE CASCADE,
  exam_id          UUID REFERENCES exam(id) ON DELETE CASCADE,
  type             TEXT NOT NULL,        -- image/pdf/video
  url              TEXT NOT NULL,        -- signed URL or path to object storage
  filename         TEXT,
  uploaded_by      UUID REFERENCES app_user(id),
  uploaded_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  -- Enforce that a file cannot be linked to both a visit and an exam at once.
  CONSTRAINT chk_attachment_link EXCLUDE USING gist (
    (visit_id IS NOT NULL)::int WITH =,
    (exam_id  IS NOT NULL)::int WITH =,
    ( (visit_id IS NOT NULL)::int + (exam_id IS NOT NULL)::int ) WITH =
  ) DEFERRABLE INITIALLY DEFERRED
);
-- Note: If you prefer a simpler constraint, replace the EXCLUDE with:
-- CHECK (NOT (visit_id IS NOT NULL AND exam_id IS NOT NULL));

-- =========================
-- Audit Log (polymorphic)
-- =========================
CREATE TABLE audit_log (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  actor_user_id    UUID REFERENCES app_user(id) ON DELETE SET NULL,
  entity_type      TEXT NOT NULL,              -- e.g., 'patient','visit','exam'
  entity_id        UUID NOT NULL,
  action           TEXT NOT NULL,              -- 'create','update','delete'
  diff_snapshot    JSONB,
  occurred_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  ip               INET
);

-- =====================================================================
-- Indexes (search & JSON performance)
-- =====================================================================

-- Case-insensitive email already handled by CITEXT uniqueness

-- Patient search
CREATE INDEX IF NOT EXISTS idx_patient_name_trgm
  ON patient USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_patient_microchip
  ON patient (microchip_id);
CREATE INDEX IF NOT EXISTS idx_patient_owner
  ON patient (owner_id);

-- Owner quick search
CREATE INDEX IF NOT EXISTS idx_owner_name_trgm
  ON owner USING gin (name gin_trgm_ops);

-- Visits
CREATE INDEX IF NOT EXISTS idx_visit_patient
  ON visit (patient_id, date_time DESC);

-- Medications
CREATE INDEX IF NOT EXISTS idx_med_patient
  ON medication (patient_id);
CREATE INDEX IF NOT EXISTS idx_med_next_due
  ON medication (next_due_at);

-- Reminders
CREATE INDEX IF NOT EXISTS idx_reminder_due
  ON reminder (status, due_at);

-- Exams
CREATE INDEX IF NOT EXISTS idx_exam_patient_time
  ON exam (patient_id, performed_at DESC);
CREATE INDEX IF NOT EXISTS idx_exam_template
  ON exam (template_id, template_version);

-- JSONB indexes (Postgres)
CREATE INDEX IF NOT EXISTS idx_exam_results_json_gin
  ON exam USING gin (results_json jsonb_path_ops);
CREATE INDEX IF NOT EXISTS idx_exam_vitals_json_gin
  ON exam USING gin (vitals_json jsonb_path_ops);
CREATE INDEX IF NOT EXISTS idx_visit_vitals_json_gin
  ON visit USING gin (vitals_json jsonb_path_ops);

-- Attachments
CREATE INDEX IF NOT EXISTS idx_attachment_patient
  ON attachment (patient_id);
CREATE INDEX IF NOT EXISTS idx_attachment_visit
  ON attachment (visit_id);
CREATE INDEX IF NOT EXISTS idx_attachment_exam
  ON attachment (exam_id);

-- Audit
CREATE INDEX IF NOT EXISTS idx_audit_entity
  ON audit_log (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_actor_time
  ON audit_log (actor_user_id, occurred_at DESC);

-- =====================================================================
-- Triggers to keep updated_at fresh (optional but handy)
-- =====================================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_owner_updated    BEFORE UPDATE ON owner      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_patient_updated  BEFORE UPDATE ON patient    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_visit_updated    BEFORE UPDATE ON visit      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_med_updated      BEFORE UPDATE ON medication FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_rem_updated      BEFORE UPDATE ON reminder   FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_examtpl_updated  BEFORE UPDATE ON exam_template FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_exam_updated     BEFORE UPDATE ON exam       FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_user_updated     BEFORE UPDATE ON app_user   FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =====================================================================
-- Helpful comments
-- =====================================================================
COMMENT ON TABLE exam_template IS 'Immutable by version: edit by creating a new row with version+1; keep history intact.';
COMMENT ON COLUMN exam.template_version IS 'Copied from exam_template.version at creation time for audit stability.';
COMMENT ON TABLE attachment IS 'Patient-level file always requires patient_id; optionally linked to a visit OR an exam (not both).';

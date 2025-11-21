-- =====================================================================
-- VetTrack — Common Seed Data (Production-Safe)
-- =====================================================================
-- This seed runs in ALL environments (dev, staging, prod)
-- Keep this minimal and essential only!

-- =====================================================================
-- Admin User
-- =====================================================================
-- NOTE: In production, replace this password hash with a secure one!
-- Current hash is for password: "admin123" (bcrypt, 10 rounds)
INSERT INTO app_user (name, email, role, password_hash, status)
VALUES (
  'System Administrator',
  'admin@vettrack.local',
  'admin',
  '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  TRUE
)
ON CONFLICT (email) DO NOTHING;

-- =====================================================================
-- Exam Templates
-- =====================================================================
-- Base exam templates that should exist in production

INSERT INTO exam_template (name, version, description, is_active, fields_json)
VALUES
  (
    'General Checkup',
    1,
    'Routine general physical examination',
    TRUE,
    '[
      {"key": "weight", "label": "Weight (kg)", "type": "number", "required": true},
      {"key": "temperature", "label": "Temperature (°C)", "type": "number", "required": true},
      {"key": "heart_rate", "label": "Heart Rate (bpm)", "type": "number", "required": true},
      {"key": "respiratory_rate", "label": "Respiratory Rate", "type": "number", "required": true},
      {"key": "mucous_membranes", "label": "Mucous Membranes", "type": "select", "options": ["Pink", "Pale", "Cyanotic", "Icteric"], "required": true},
      {"key": "capillary_refill", "label": "Capillary Refill Time (CRT)", "type": "select", "options": ["<2s", "2-3s", ">3s"], "required": true},
      {"key": "hydration", "label": "Hydration Status", "type": "select", "options": ["Normal", "Mild Dehydration", "Moderate Dehydration", "Severe Dehydration"], "required": true},
      {"key": "body_condition", "label": "Body Condition Score (1-9)", "type": "number", "required": true},
      {"key": "cardiovascular", "label": "Cardiovascular System", "type": "textarea", "required": false},
      {"key": "respiratory", "label": "Respiratory System", "type": "textarea", "required": false},
      {"key": "gastrointestinal", "label": "Gastrointestinal System", "type": "textarea", "required": false},
      {"key": "musculoskeletal", "label": "Musculoskeletal System", "type": "textarea", "required": false},
      {"key": "neurological", "label": "Neurological Assessment", "type": "textarea", "required": false},
      {"key": "skin_coat", "label": "Skin and Coat", "type": "textarea", "required": false},
      {"key": "findings", "label": "Additional Findings", "type": "textarea", "required": false}
    ]'::jsonb
  ),
  (
    'Vaccination Record',
    1,
    'Vaccine administration and tracking',
    TRUE,
    '[
      {"key": "vaccine_name", "label": "Vaccine Name", "type": "text", "required": true},
      {"key": "manufacturer", "label": "Manufacturer", "type": "text", "required": true},
      {"key": "lot_number", "label": "Lot Number", "type": "text", "required": true},
      {"key": "expiration_date", "label": "Expiration Date", "type": "date", "required": true},
      {"key": "route", "label": "Route of Administration", "type": "select", "options": ["SC", "IM", "IV", "PO", "IN"], "required": true},
      {"key": "site", "label": "Injection Site", "type": "text", "required": false},
      {"key": "dose", "label": "Dose", "type": "text", "required": true},
      {"key": "next_due_date", "label": "Next Due Date", "type": "date", "required": false},
      {"key": "adverse_reactions", "label": "Adverse Reactions Observed", "type": "textarea", "required": false},
      {"key": "notes", "label": "Additional Notes", "type": "textarea", "required": false}
    ]'::jsonb
  ),
  (
    'Radiographic Exam',
    1,
    'X-ray imaging study documentation',
    TRUE,
    '[
      {"key": "study_type", "label": "Study Type", "type": "select", "options": ["Thorax", "Abdomen", "Spine", "Limbs", "Skull", "Pelvis", "Other"], "required": true},
      {"key": "views", "label": "Views Taken", "type": "text", "required": true},
      {"key": "positioning", "label": "Patient Positioning", "type": "text", "required": false},
      {"key": "technique", "label": "Technique (kV/mAs)", "type": "text", "required": false},
      {"key": "contrast", "label": "Contrast Used", "type": "select", "options": ["None", "Barium", "Iodine", "Other"], "required": false},
      {"key": "quality", "label": "Image Quality", "type": "select", "options": ["Excellent", "Good", "Fair", "Poor", "Non-diagnostic"], "required": true},
      {"key": "findings", "label": "Radiographic Findings", "type": "textarea", "required": true},
      {"key": "impression", "label": "Radiographic Impression", "type": "textarea", "required": true},
      {"key": "recommendations", "label": "Recommendations", "type": "textarea", "required": false}
    ]'::jsonb
  ),
  (
    'Dental Exam',
    1,
    'Oral and dental health assessment',
    TRUE,
    '[
      {"key": "dental_chart", "label": "Dental Chart", "type": "textarea", "required": false},
      {"key": "calculus", "label": "Calculus Grade (0-4)", "type": "number", "required": true},
      {"key": "gingivitis", "label": "Gingivitis Grade (0-4)", "type": "number", "required": true},
      {"key": "periodontal_disease", "label": "Periodontal Disease Stage", "type": "select", "options": ["None", "Stage 1", "Stage 2", "Stage 3", "Stage 4"], "required": true},
      {"key": "missing_teeth", "label": "Missing Teeth", "type": "text", "required": false},
      {"key": "fractured_teeth", "label": "Fractured Teeth", "type": "text", "required": false},
      {"key": "oral_masses", "label": "Oral Masses/Lesions", "type": "textarea", "required": false},
      {"key": "extractions", "label": "Teeth Extracted", "type": "text", "required": false},
      {"key": "scaling", "label": "Scaling Performed", "type": "select", "options": ["Yes", "No"], "required": true},
      {"key": "polishing", "label": "Polishing Performed", "type": "select", "options": ["Yes", "No"], "required": true},
      {"key": "home_care", "label": "Home Care Recommendations", "type": "textarea", "required": false}
    ]'::jsonb
  ),
  (
    'Surgical Report',
    1,
    'Documentation for surgical procedures',
    TRUE,
    '[
      {"key": "procedure", "label": "Procedure Name", "type": "text", "required": true},
      {"key": "surgeon", "label": "Surgeon", "type": "text", "required": true},
      {"key": "anesthesia_type", "label": "Anesthesia Type", "type": "text", "required": true},
      {"key": "asa_status", "label": "ASA Status", "type": "select", "options": ["I", "II", "III", "IV", "V"], "required": true},
      {"key": "pre_med", "label": "Pre-medication", "type": "text", "required": false},
      {"key": "induction", "label": "Induction Protocol", "type": "text", "required": false},
      {"key": "maintenance", "label": "Maintenance Anesthesia", "type": "text", "required": false},
      {"key": "start_time", "label": "Surgery Start Time", "type": "time", "required": true},
      {"key": "end_time", "label": "Surgery End Time", "type": "time", "required": true},
      {"key": "findings", "label": "Surgical Findings", "type": "textarea", "required": true},
      {"key": "procedure_details", "label": "Procedure Details", "type": "textarea", "required": true},
      {"key": "complications", "label": "Complications", "type": "textarea", "required": false},
      {"key": "blood_loss", "label": "Estimated Blood Loss", "type": "text", "required": false},
      {"key": "closure", "label": "Closure Technique", "type": "text", "required": false},
      {"key": "post_op_instructions", "label": "Post-operative Instructions", "type": "textarea", "required": true}
    ]'::jsonb
  )
ON CONFLICT (name, version) DO NOTHING;

-- =====================================================================
-- End of Common Seed
-- =====================================================================
-- =====================================================================
-- VetTrack — Development Seed Data
-- =====================================================================
-- This seed runs ONLY in development environments
-- Contains realistic test data for development and testing

-- =====================================================================
-- Additional Users
-- =====================================================================
-- All passwords are "password123" hashed with bcrypt (10 rounds)
INSERT INTO app_user (name, email, role, password_hash, status) VALUES
  ('Dr. Sarah Johnson', 'sarah.johnson@vettrack.local', 'vet', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', TRUE),
  ('Dr. Michael Chen', 'michael.chen@vettrack.local', 'vet', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', TRUE),
  ('Dr. Emily Rodriguez', 'emily.rodriguez@vettrack.local', 'vet', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', TRUE),
  ('Dr. James Wilson', 'james.wilson@vettrack.local', 'vet', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', TRUE),
  ('Jessica Admin', 'jessica.admin@vettrack.local', 'admin', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', TRUE)
ON CONFLICT (email) DO NOTHING;

-- =====================================================================
-- Owners
-- =====================================================================
INSERT INTO owner (id, name, phone, email) VALUES
  ('11111111-1111-1111-1111-111111111111', 'Robert Smith', '+1-555-0101', 'robert.smith@email.com'),
  ('22222222-2222-2222-2222-222222222222', 'Maria Garcia', '+1-555-0102', 'maria.garcia@email.com'),
  ('33333333-3333-3333-3333-333333333333', 'John Anderson', '+1-555-0103', 'john.anderson@email.com'),
  ('44444444-4444-4444-4444-444444444444', 'Lisa Thompson', '+1-555-0104', 'lisa.thompson@email.com'),
  ('55555555-5555-5555-5555-555555555555', 'David Martinez', '+1-555-0105', 'david.martinez@email.com'),
  ('66666666-6666-6666-6666-666666666666', 'Jennifer Lee', '+1-555-0106', 'jennifer.lee@email.com'),
  ('77777777-7777-7777-7777-777777777777', 'William Brown', '+1-555-0107', 'william.brown@email.com'),
  ('88888888-8888-8888-8888-888888888888', 'Patricia Davis', '+1-555-0108', 'patricia.davis@email.com'),
  ('99999999-9999-9999-9999-999999999999', 'Michael Johnson', '+1-555-0109', 'michael.johnson@email.com'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Sarah Wilson', '+1-555-0110', 'sarah.wilson@email.com'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'James Miller', '+1-555-0111', 'james.miller@email.com'),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Emily Taylor', '+1-555-0112', 'emily.taylor@email.com'),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Christopher Moore', '+1-555-0113', 'christopher.moore@email.com'),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Amanda White', '+1-555-0114', 'amanda.white@email.com'),
  ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Daniel Harris', '+1-555-0115', 'daniel.harris@email.com')
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Patients
-- =====================================================================
INSERT INTO patient (id, owner_id, name, species, breed, sex, dob, color, microchip_id, allergies, notes) VALUES
  -- Robert Smith's pets
  ('10000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Max', 'Canine', 'Golden Retriever', 'Male', '2019-03-15', 'Golden', '985112001234567', NULL, 'Very friendly, loves treats'),
  ('10000001-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'Luna', 'Feline', 'Domestic Shorthair', 'Female', '2020-07-22', 'Tabby', '985112001234568', 'Penicillin', 'Indoor only, shy with strangers'),
  
  -- Maria Garcia's pets
  ('10000002-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', 'Bella', 'Canine', 'Labrador Retriever', 'Female', '2018-11-08', 'Chocolate', '985112001234569', NULL, 'Energetic, hip dysplasia monitoring'),
  ('10000002-0000-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222', 'Charlie', 'Canine', 'Beagle', 'Male', '2021-01-30', 'Tri-color', '985112001234570', NULL, 'Food motivated, prone to ear infections'),
  
  -- John Anderson's pets
  ('10000003-0000-0000-0000-000000000001', '33333333-3333-3333-3333-333333333333', 'Milo', 'Feline', 'Maine Coon', 'Male', '2017-05-12', 'Brown Tabby', '985112001234571', NULL, 'Outdoor cat, vaccinated for FeLV'),
  ('10000003-0000-0000-0000-000000000002', '33333333-3333-3333-3333-333333333333', 'Daisy', 'Canine', 'Poodle', 'Female', '2022-09-03', 'White', '985112001234572', 'Sulfa drugs', 'Requires grooming every 6 weeks'),
  
  -- Lisa Thompson's pets
  ('10000004-0000-0000-0000-000000000001', '44444444-4444-4444-4444-444444444444', 'Rocky', 'Canine', 'German Shepherd', 'Male', '2019-08-25', 'Black and Tan', '985112001234573', NULL, 'Working dog, excellent temperament'),
  ('10000004-0000-0000-0000-000000000002', '44444444-4444-4444-4444-444444444444', 'Whiskers', 'Feline', 'Siamese', 'Male', '2020-12-10', 'Seal Point', '985112001234574', NULL, 'Vocal, diabetes monitoring'),
  
  -- David Martinez's pets
  ('10000005-0000-0000-0000-000000000001', '55555555-5555-5555-5555-555555555555', 'Cooper', 'Canine', 'Border Collie', 'Male', '2020-04-18', 'Black and White', '985112001234575', NULL, 'High energy, agility training'),
  ('10000005-0000-0000-0000-000000000002', '55555555-5555-5555-5555-555555555555', 'Shadow', 'Feline', 'Persian', 'Female', '2019-02-28', 'Gray', '985112001234576', NULL, 'Requires daily eye cleaning'),
  
  -- Jennifer Lee's pets
  ('10000006-0000-0000-0000-000000000001', '66666666-6666-6666-6666-666666666666', 'Buddy', 'Canine', 'French Bulldog', 'Male', '2021-06-14', 'Fawn', '985112001234577', NULL, 'Brachycephalic breed, monitor breathing'),
  ('10000006-0000-0000-0000-000000000002', '66666666-6666-6666-6666-666666666666', 'Cleo', 'Feline', 'Bengal', 'Female', '2020-10-05', 'Brown Spotted', '985112001234578', NULL, 'Very active, requires enrichment'),
  
  -- William Brown's pets
  ('10000007-0000-0000-0000-000000000001', '77777777-7777-7777-7777-777777777777', 'Sadie', 'Canine', 'Cocker Spaniel', 'Female', '2018-03-20', 'Buff', '985112001234579', 'NSAIDs', 'Chronic ear issues'),
  ('10000007-0000-0000-0000-000000000002', '77777777-7777-7777-7777-777777777777', 'Tiger', 'Feline', 'Orange Tabby', 'Male', '2019-09-15', 'Orange', '985112001234580', NULL, 'Neutered, indoor/outdoor'),
  
  -- Patricia Davis's pets
  ('10000008-0000-0000-0000-000000000001', '88888888-8888-8888-8888-888888888888', 'Duke', 'Canine', 'Boxer', 'Male', '2020-01-07', 'Brindle', '985112001234581', NULL, 'Heart murmur, annual echocardiogram'),
  ('10000008-0000-0000-0000-000000000002', '88888888-8888-8888-8888-888888888888', 'Mittens', 'Feline', 'Ragdoll', 'Female', '2021-11-22', 'Blue Point', '985112001234582', NULL, 'Indoor only, very docile'),
  
  -- Michael Johnson's pets
  ('10000009-0000-0000-0000-000000000001', '99999999-9999-9999-9999-999999999999', 'Zeus', 'Canine', 'Rottweiler', 'Male', '2019-07-30', 'Black and Tan', '985112001234583', NULL, 'Well-trained, protective'),
  ('10000009-0000-0000-0000-000000000002', '99999999-9999-9999-9999-999999999999', 'Simba', 'Feline', 'British Shorthair', 'Male', '2020-05-18', 'Blue', '985112001234584', NULL, 'Calm temperament, overweight'),
  
  -- Sarah Wilson's pets
  ('1000000a-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Bailey', 'Canine', 'Australian Shepherd', 'Female', '2021-02-11', 'Blue Merle', '985112001234585', NULL, 'Working line, herding instinct'),
  ('1000000a-0000-0000-0000-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Oliver', 'Feline', 'Russian Blue', 'Male', '2019-12-03', 'Gray-Blue', '985112001234586', NULL, 'Shy, prefers quiet environment'),
  
  -- James Miller's pets
  ('1000000b-0000-0000-0000-000000000001', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Molly', 'Canine', 'Shih Tzu', 'Female', '2018-08-28', 'Gold and White', '985112001234587', NULL, 'Dental issues, regular cleanings'),
  ('1000000b-0000-0000-0000-000000000002', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Gizmo', 'Feline', 'Sphynx', 'Male', '2020-03-16', 'Pink', '985112001234588', NULL, 'Requires bathing, temperature sensitive'),
  
  -- Emily Taylor's pets
  ('1000000c-0000-0000-0000-000000000001', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Bear', 'Canine', 'Bernese Mountain Dog', 'Male', '2019-11-19', 'Tri-color', '985112001234589', NULL, 'Large breed, joint supplements'),
  ('1000000c-0000-0000-0000-000000000002', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Lucy', 'Feline', 'Calico', 'Female', '2021-07-08', 'Calico', '985112001234590', NULL, 'Spayed, indoor only'),
  
  -- Christopher Moore's pets
  ('1000000d-0000-0000-0000-000000000001', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'Jack', 'Canine', 'Jack Russell Terrier', 'Male', '2020-09-22', 'White and Brown', '985112001234591', NULL, 'High energy, requires exercise'),
  ('1000000d-0000-0000-0000-000000000002', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'Nala', 'Feline', 'Abyssinian', 'Female', '2019-04-14', 'Ruddy', '985112001234592', NULL, 'Active, playful temperament'),
  
  -- Amanda White's pets
  ('1000000e-0000-0000-0000-000000000001', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Rusty', 'Canine', 'Irish Setter', 'Male', '2018-12-05', 'Red', '985112001234593', NULL, 'Athletic, field trial dog'),
  ('1000000e-0000-0000-0000-000000000002', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Smokey', 'Feline', 'Chartreux', 'Male', '2021-03-27', 'Blue-Gray', '985112001234594', NULL, 'Quiet, gentle disposition'),
  
  -- Daniel Harris's pets
  ('1000000f-0000-0000-0000-000000000001', 'ffffffff-ffff-ffff-ffff-ffffffffffff', 'Ace', 'Canine', 'Doberman Pinscher', 'Male', '2019-05-09', 'Black and Rust', '985112001234595', NULL, 'Guard dog training, excellent obedience'),
  ('1000000f-0000-0000-0000-000000000002', 'ffffffff-ffff-ffff-ffff-ffffffffffff', 'Princess', 'Feline', 'Himalayan', 'Female', '2020-08-31', 'Chocolate Point', '985112001234596', NULL, 'Requires regular grooming, matting issues')
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Visits (Recent and Historical)
-- =====================================================================
INSERT INTO visit (id, patient_id, date_time, reason, vitals_json, exam_notes, diagnoses, procedures, recommendations, created_by) VALUES
  -- Max's visits
  ('20000001-0001-0000-0000-000000000001', '10000001-0000-0000-0000-000000000001', '2024-11-15 10:30:00+00', 'Annual wellness exam', 
   '{"weight": 32.5, "temperature": 38.6, "heart_rate": 95, "respiratory_rate": 24}'::jsonb,
   'Bright, alert, and responsive. Good body condition. No abnormalities detected on physical exam.',
   'Healthy adult dog',
   'Annual vaccinations administered (DHPP, Rabies)',
   'Continue current diet and exercise. Return in 1 year for next wellness exam.',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local')),
  
  ('20000001-0001-0000-0000-000000000002', '10000001-0000-0000-0000-000000000001', '2024-08-22 14:15:00+00', 'Limping on right front leg',
   '{"weight": 32.8, "temperature": 38.7, "heart_rate": 110, "respiratory_rate": 28}'::jsonb,
   'Mild lameness grade 2/5 on right forelimb. Pain on palpation of carpus. No swelling noted.',
   'Mild carpal sprain',
   'Radiographs of right carpus (2 views)',
   'Rest for 2 weeks. Carprofen 75mg BID x 7 days. Recheck if not improved.',
   (SELECT id FROM app_user WHERE email = 'michael.chen@vettrack.local')),
  
  -- Luna's visits
  ('20000001-0002-0000-0000-000000000001', '10000001-0000-0000-0000-000000000002', '2024-10-30 09:00:00+00', 'Vomiting for 2 days',
   '{"weight": 4.2, "temperature": 39.1, "heart_rate": 180, "respiratory_rate": 32}'::jsonb,
   'Mild dehydration (5%). Abdominal palpation reveals mild discomfort. No foreign body palpable.',
   'Acute gastroenteritis',
   'Subcutaneous fluids (100ml LRS), Cerenia injection',
   'Bland diet for 3 days. Probiotics. Monitor for continued vomiting. Recheck in 3 days if not improved.',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local')),
  
  -- Bella's visits
  ('20000002-0001-0000-0000-000000000001', '10000002-0000-0000-0000-000000000001', '2024-11-10 11:00:00+00', 'Skin allergies - itching',
   '{"weight": 29.3, "temperature": 38.5, "heart_rate": 88, "respiratory_rate": 22}'::jsonb,
   'Moderate erythema and excoriation on ventral abdomen and paws. No evidence of fleas. Ears clear.',
   'Atopic dermatitis, likely environmental allergies',
   'Skin scraping (negative for mites), Ear cleaning, Cytopoint injection',
   'Start daily Apoquel 16mg. Omega-3 fatty acid supplement. Consider allergy testing if recurrent.',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  -- Charlie's visits
  ('20000002-0002-0000-0000-000000000001', '10000002-0000-0000-0000-000000000002', '2024-11-05 15:30:00+00', 'Routine check-up and ear cleaning',
   '{"weight": 11.8, "temperature": 38.4, "heart_rate": 92, "respiratory_rate": 26}'::jsonb,
   'Bilateral otitis externa. Ears have brown waxy discharge with mild erythema. Otherwise healthy.',
   'Bilateral otitis externa (yeast)',
   'Ear cleaning and cytology, Prescribed Otomax otic solution',
   'Apply Otomax to both ears BID x 14 days. Recheck ears in 2 weeks.',
   (SELECT id FROM app_user WHERE email = 'james.wilson@vettrack.local')),
  
  -- Milo's visits
  ('20000003-0001-0000-0000-000000000001', '10000003-0000-0000-0000-000000000001', '2024-09-18 10:45:00+00', 'Not eating well, weight loss',
   '{"weight": 5.8, "temperature": 38.3, "heart_rate": 165, "respiratory_rate": 30}'::jsonb,
   'Cat appears thin (BCS 3/9). Dental exam reveals severe tartar and gingivitis. Multiple teeth mobile.',
   'Severe periodontal disease, likely causing oral pain and inappetence',
   'Dental radiographs recommended (declined by owner), Prescribed antibiotics',
   'Clavamox 62.5mg BID x 14 days. Dental cleaning under anesthesia strongly recommended. Recheck in 2 weeks.',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local')),
  
  -- Rocky's visits
  ('20000004-0001-0000-0000-000000000001', '10000004-0000-0000-0000-000000000001', '2024-11-12 13:00:00+00', 'Annual wellness and hip evaluation',
   '{"weight": 38.5, "temperature": 38.7, "heart_rate": 85, "heart_rate": 20}'::jsonb,
   'Overall excellent condition. Hip laxity minimal on Ortolani test. No crepitus. Strong muscling.',
   'Healthy adult dog, no signs of hip dysplasia',
   'Hip radiographs (OFA preliminary), Annual vaccines, Heartworm test (negative)',
   'Continue joint supplement. Maintain lean body weight. Annual follow-up.',
   (SELECT id FROM app_user WHERE email = 'michael.chen@vettrack.local')),
  
  -- Whiskers' visits
  ('20000004-0002-0000-0000-000000000001', '10000004-0000-0000-0000-000000000002', '2024-11-08 09:30:00+00', 'Diabetes recheck and glucose curve',
   '{"weight": 5.1, "temperature": 38.2, "heart_rate": 175, "respiratory_rate": 28}'::jsonb,
   'Patient doing well on insulin. Owner reports decreased polyuria/polydipsia. BCS 5/9.',
   'Diabetes mellitus - well controlled',
   'Blood glucose curve performed (see lab results)',
   'Continue Lantus 2 units BID. Recheck fructosamine in 1 month. Owner monitoring well.',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  -- Cooper's visits
  ('20000005-0001-0000-0000-000000000001', '10000005-0000-0000-0000-000000000001', '2024-10-25 14:00:00+00', 'Pre-competition health check',
   '{"weight": 18.2, "temperature": 38.5, "heart_rate": 78, "respiratory_rate": 22}'::jsonb,
   'Excellent athletic condition. Cardiovascular exam normal. Musculoskeletal exam unremarkable.',
   'Healthy working dog',
   'Full physical exam, Fecal examination (negative)',
   'Cleared for competition. Continue current training regimen.',
   (SELECT id FROM app_user WHERE email = 'james.wilson@vettrack.local')),
  
  -- Buddy's visits
  ('20000006-0001-0000-0000-000000000001', '10000006-0000-0000-0000-000000000001', '2024-11-01 10:00:00+00', 'Difficulty breathing after exercise',
   '{"weight": 12.5, "temperature": 38.9, "heart_rate": 135, "respiratory_rate": 40}'::jsonb,
   'Brachycephalic airway syndrome evident. Stenotic nares, elongated soft palate suspected. Respiratory distress mild.',
   'Brachycephalic obstructive airway syndrome (BOAS)',
   'Physical exam, Thoracic auscultation',
   'Limit exercise in heat. Consider surgical correction (nares, soft palate). Maintain lean weight. Referral to surgeon if worsening.',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local'))
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Medications (Active and Recent)
-- =====================================================================
INSERT INTO medication (id, patient_id, name, dosage, route, frequency, start_date, end_date, next_due_at, created_by) VALUES
  -- Bella's allergy medication
  ('30000001-0000-0000-0000-000000000001', '10000002-0000-0000-0000-000000000001', 'Apoquel', '16 mg', 'PO', 'BID', '2024-11-10', NULL, '2024-11-19 08:00:00+00',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  ('30000001-0000-0000-0000-000000000002', '10000002-0000-0000-0000-000000000001', 'Omega-3 Fatty Acids', '1000 mg', 'PO', 'SID', '2024-11-10', NULL, '2024-11-19 08:00:00+00',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  -- Charlie's ear infection treatment
  ('30000002-0000-0000-0000-000000000001', '10000002-0000-0000-0000-000000000002', 'Otomax', '0.5 ml', 'Otic', 'BID', '2024-11-05', '2024-11-19', '2024-11-19 08:00:00+00',
   (SELECT id FROM app_user WHERE email = 'james.wilson@vettrack.local')),
  
  -- Whiskers' insulin
  ('30000003-0000-0000-0000-000000000001', '10000004-0000-0000-0000-000000000002', 'Lantus (glargine)', '2 units', 'SQ', 'BID', '2024-06-15', NULL, '2024-11-19 08:00:00+00',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  -- Rocky's joint supplement
  ('30000004-0000-0000-0000-000000000001', '10000004-0000-0000-0000-000000000001', 'Cosequin DS', '1 tablet', 'PO', 'SID', '2023-01-15', NULL, '2024-11-19 08:00:00+00',
   (SELECT id FROM app_user WHERE email = 'michael.chen@vettrack.local')),
  
  -- Duke's heart medication
  ('30000005-0000-0000-0000-000000000001', '10000008-0000-0000-0000-000000000001', 'Enalapril', '10 mg', 'PO', 'BID', '2024-03-20', NULL, '2024-11-19 08:00:00+00',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local')),
  
  ('30000005-0000-0000-0000-000000000002', '10000008-0000-0000-0000-000000000001', 'Pimobendan', '5 mg', 'PO', 'BID', '2024-03-20', NULL, '2024-11-19 08:00:00+00',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local')),
  
  -- Sadie's ear medication
  ('30000006-0000-0000-0000-000000000001', '10000007-0000-0000-0000-000000000001', 'Zymox Otic', '1 ml', 'Otic', 'SID', '2024-11-01', '2024-11-15', NULL,
   (SELECT id FROM app_user WHERE email = 'james.wilson@vettrack.local'))
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Reminders
-- =====================================================================
INSERT INTO reminder (id, patient_id, title, due_at, status, created_by) VALUES
  -- Upcoming reminders
  ('40000001-0000-0000-0000-000000000001', '10000001-0000-0000-0000-000000000001', 'Annual wellness exam', '2025-11-15 10:00:00+00', 'pending',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local')),
  
  ('40000002-0000-0000-0000-000000000001', '10000002-0000-0000-0000-000000000002', 'Ear recheck', '2024-11-19 15:00:00+00', 'pending',
   (SELECT id FROM app_user WHERE email = 'james.wilson@vettrack.local')),
  
  ('40000003-0000-0000-0000-000000000001', '10000003-0000-0000-0000-000000000001', 'Dental cleaning recommended', '2024-12-01 09:00:00+00', 'pending',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local')),
  
  ('40000004-0000-0000-0000-000000000001', '10000004-0000-0000-0000-000000000002', 'Diabetes recheck - fructosamine', '2024-12-08 09:30:00+00', 'pending',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  ('40000005-0000-0000-0000-000000000001', '10000005-0000-0000-0000-000000000001', 'Heartworm prevention refill', '2024-11-25 00:00:00+00', 'pending',
   (SELECT id FROM app_user WHERE email = 'james.wilson@vettrack.local')),
  
  ('40000006-0000-0000-0000-000000000001', '10000006-0000-0000-0000-000000000001', 'Brachycephalic surgery consult', '2024-12-15 10:00:00+00', 'pending',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local')),
  
  ('40000007-0000-0000-0000-000000000001', '10000001-0000-0000-0000-000000000002', 'Gastroenteritis follow-up', '2024-11-20 09:00:00+00', 'pending',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local')),
  
  ('40000008-0000-0000-0000-000000000001', '10000008-0000-0000-0000-000000000001', 'Echocardiogram - annual', '2025-03-20 14:00:00+00', 'pending',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local')),
  
  -- Overdue reminders
  ('40000009-0000-0000-0000-000000000001', '10000007-0000-0000-0000-000000000002', 'Rabies vaccination due', '2024-11-10 00:00:00+00', 'overdue',
   (SELECT id FROM app_user WHERE email = 'michael.chen@vettrack.local')),
  
  ('40000010-0000-0000-0000-000000000001', '10000009-0000-0000-0000-000000000002', 'Weight management follow-up', '2024-11-05 00:00:00+00', 'overdue',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  -- Completed reminders
  ('40000011-0000-0000-0000-000000000001', '10000002-0000-0000-0000-000000000001', 'Allergy treatment follow-up', '2024-11-10 11:00:00+00', 'done',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  ('40000012-0000-0000-0000-000000000001', '10000004-0000-0000-0000-000000000001', 'Hip radiographs', '2024-11-12 13:00:00+00', 'done',
   (SELECT id FROM app_user WHERE email = 'michael.chen@vettrack.local'))
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Exams (Linked to Visits)
-- =====================================================================
INSERT INTO exam (id, patient_id, visit_id, template_id, template_version, performed_at, performed_by, vitals_json, results_json, status, notes) VALUES
  -- Max's annual wellness exam
  ('50000001-0000-0000-0000-000000000001', 
   '10000001-0000-0000-0000-000000000001',
   '20000001-0001-0000-0000-000000000001',
   (SELECT id FROM exam_template WHERE name = 'General Checkup' AND version = 1),
   1,
   '2024-11-15 10:30:00+00',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local'),
   '{"weight": 32.5, "temperature": 38.6, "heart_rate": 95, "respiratory_rate": 24}'::jsonb,
   '{
     "weight": 32.5,
     "temperature": 38.6,
     "heart_rate": 95,
     "respiratory_rate": 24,
     "mucous_membranes": "Pink",
     "capillary_refill": "<2s",
     "hydration": "Normal",
     "body_condition": 5,
     "cardiovascular": "Heart sounds normal, no murmurs detected. Pulses strong and synchronous.",
     "respiratory": "Clear lung sounds bilaterally. No cough or respiratory effort.",
     "gastrointestinal": "Abdomen soft and non-painful. Normal bowel sounds.",
     "musculoskeletal": "Good muscle tone. Full range of motion all limbs. No lameness.",
     "neurological": "Alert and responsive. Cranial nerves intact. Normal gait.",
     "skin_coat": "Healthy coat. No evidence of parasites or skin lesions.",
     "findings": "Overall healthy adult dog. All parameters within normal limits."
   }'::jsonb,
   'final',
   'Owner reports patient is doing well at home. No concerns.'
  ),
  
  -- Bella's allergy exam
  ('50000002-0000-0000-0000-000000000001',
   '10000002-0000-0000-0000-000000000001',
   '20000002-0001-0000-0000-000000000001',
   (SELECT id FROM exam_template WHERE name = 'General Checkup' AND version = 1),
   1,
   '2024-11-10 11:00:00+00',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local'),
   '{"weight": 29.3, "temperature": 38.5, "heart_rate": 88, "respiratory_rate": 22}'::jsonb,
   '{
     "weight": 29.3,
     "temperature": 38.5,
     "heart_rate": 88,
     "respiratory_rate": 22,
     "mucous_membranes": "Pink",
     "capillary_refill": "<2s",
     "hydration": "Normal",
     "body_condition": 5,
     "cardiovascular": "Normal heart sounds. No murmurs.",
     "respiratory": "Clear lung sounds.",
     "gastrointestinal": "Normal",
     "musculoskeletal": "Normal",
     "neurological": "Normal",
     "skin_coat": "Moderate erythema on ventral abdomen, axillae, and interdigital spaces. Self-trauma evident with excoriation. No parasites visualized. Ears clean.",
     "findings": "Clinical signs consistent with atopic dermatitis. Pruritus score 7/10 per owner."
   }'::jsonb,
   'final',
   'Discussed long-term allergy management options with owner.'
  ),
  
  -- Rocky's hip evaluation
  ('50000003-0000-0000-0000-000000000001',
   '10000004-0000-0000-0000-000000000001',
   '20000004-0001-0000-0000-000000000001',
   (SELECT id FROM exam_template WHERE name = 'Radiographic Exam' AND version = 1),
   1,
   '2024-11-12 13:00:00+00',
   (SELECT id FROM app_user WHERE email = 'michael.chen@vettrack.local'),
   '{"weight": 38.5, "temperature": 38.7, "heart_rate": 85, "respiratory_rate": 20}'::jsonb,
   '{
     "study_type": "Pelvis",
     "views": "VD extended hip view (OFA positioning)",
     "positioning": "Patient anesthetized, hind limbs extended and internally rotated",
     "technique": "70 kV / 10 mAs",
     "contrast": "None",
     "quality": "Excellent",
     "findings": "Bilateral hip joints well-positioned. Femoral heads well-seated in acetabula. No evidence of subluxation. Joint spaces symmetric. No evidence of degenerative changes. Norberg angles within normal limits bilaterally.",
     "impression": "No radiographic evidence of hip dysplasia. Preliminary OFA evaluation: Excellent hip conformation.",
     "recommendations": "Submit radiographs to OFA for official certification. Continue joint supplementation and weight management."
   }'::jsonb,
   'final',
   'Owner very pleased with results. Will submit to OFA.'
  ),
  
  -- Whiskers' diabetes exam
  ('50000004-0000-0000-0000-000000000001',
   '10000004-0000-0000-0000-000000000002',
   '20000004-0002-0000-0000-000000000001',
   (SELECT id FROM exam_template WHERE name = 'General Checkup' AND version = 1),
   1,
   '2024-11-08 09:30:00+00',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local'),
   '{"weight": 5.1, "temperature": 38.2, "heart_rate": 175, "respiratory_rate": 28}'::jsonb,
   '{
     "weight": 5.1,
     "temperature": 38.2,
     "heart_rate": 175,
     "respiratory_rate": 28,
     "mucous_membranes": "Pink",
     "capillary_refill": "<2s",
     "hydration": "Normal",
     "body_condition": 5,
     "cardiovascular": "Normal heart sounds.",
     "respiratory": "Clear lung sounds.",
     "gastrointestinal": "Normal appetite per owner. No vomiting or diarrhea.",
     "musculoskeletal": "Normal",
     "neurological": "Normal gait. No evidence of diabetic neuropathy.",
     "skin_coat": "Coat quality good. No thin skin noted.",
     "findings": "Glucose curve performed: Pre-insulin 215, +2hr 145, +4hr 98, +6hr 125, +8hr 178, +10hr 210, +12hr 245. Nadir appropriate, duration good. Cat clinically well-regulated."
   }'::jsonb,
   'final',
   'Excellent diabetes control. Owner doing great job with monitoring and insulin administration.'
  ),
  
  -- Max's vaccination record
  ('50000005-0000-0000-0000-000000000001',
   '10000001-0000-0000-0000-000000000001',
   '20000001-0001-0000-0000-000000000001',
   (SELECT id FROM exam_template WHERE name = 'Vaccination Record' AND version = 1),
   1,
   '2024-11-15 10:45:00+00',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local'),
   NULL,
   '{
     "vaccine_name": "DHPP (Distemper, Hepatitis, Parvovirus, Parainfluenza)",
     "manufacturer": "Zoetis",
     "lot_number": "4023587A",
     "expiration_date": "2025-10-15",
     "route": "SC",
     "site": "Right lateral thorax",
     "dose": "1.0 ml",
     "next_due_date": "2025-11-15",
     "adverse_reactions": "None observed",
     "notes": "Vaccine administered without complications. Owner advised to monitor for any reactions."
   }'::jsonb,
   'final',
   NULL
  ),
  
  ('50000005-0000-0000-0000-000000000002',
   '10000001-0000-0000-0000-000000000001',
   '20000001-0001-0000-0000-000000000001',
   (SELECT id FROM exam_template WHERE name = 'Vaccination Record' AND version = 1),
   1,
   '2024-11-15 10:47:00+00',
   (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local'),
   NULL,
   '{
     "vaccine_name": "Rabies",
     "manufacturer": "Boehringer Ingelheim",
     "lot_number": "RAB892KL",
     "expiration_date": "2026-08-20",
     "route": "SC",
     "site": "Right rear limb",
     "dose": "1.0 ml",
     "next_due_date": "2027-11-15",
     "adverse_reactions": "None observed",
     "notes": "3-year rabies vaccine administered. Rabies certificate issued to owner."
   }'::jsonb,
   'final',
   NULL
  )
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Dose Events (Medication Administration History)
-- =====================================================================
INSERT INTO dose_event (medication_id, administered_at, amount, notes, recorded_by) VALUES
  -- Whiskers' insulin doses (last 3 days)
  ((SELECT id FROM medication WHERE patient_id = '10000004-0000-0000-0000-000000000002' AND name = 'Lantus (glargine)'), '2024-11-18 08:00:00+00', '2 units', 'Morning dose', (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  ((SELECT id FROM medication WHERE patient_id = '10000004-0000-0000-0000-000000000002' AND name = 'Lantus (glargine)'), '2024-11-18 20:00:00+00', '2 units', 'Evening dose', (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  ((SELECT id FROM medication WHERE patient_id = '10000004-0000-0000-0000-000000000002' AND name = 'Lantus (glargine)'), '2024-11-17 08:00:00+00', '2 units', 'Morning dose', (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  ((SELECT id FROM medication WHERE patient_id = '10000004-0000-0000-0000-000000000002' AND name = 'Lantus (glargine)'), '2024-11-17 20:00:00+00', '2 units', 'Evening dose', (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  ((SELECT id FROM medication WHERE patient_id = '10000004-0000-0000-0000-000000000002' AND name = 'Lantus (glargine)'), '2024-11-16 08:00:00+00', '2 units', 'Morning dose', (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  ((SELECT id FROM medication WHERE patient_id = '10000004-0000-0000-0000-000000000002' AND name = 'Lantus (glargine)'), '2024-11-16 20:00:00+00', '2 units', 'Evening dose', (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  -- Duke's heart medications
  ((SELECT id FROM medication WHERE patient_id = '10000008-0000-0000-0000-000000000001' AND name = 'Enalapril'), '2024-11-18 08:00:00+00', '10 mg', 'With food', (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local')),
  ((SELECT id FROM medication WHERE patient_id = '10000008-0000-0000-0000-000000000001' AND name = 'Pimobendan'), '2024-11-18 08:00:00+00', '5 mg', 'One hour before food', (SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local'))
ON CONFLICT DO NOTHING;

-- =====================================================================
-- Attachments (Sample files - URLs would be real S3/storage links in production)
-- =====================================================================
INSERT INTO attachment (patient_id, visit_id, exam_id, type, url, filename, uploaded_by) VALUES
  -- Max's radiographs
  ('10000001-0000-0000-0000-000000000001', '20000001-0001-0000-0000-000000000002', NULL, 'image/jpeg', 
   'https://storage.vettrack.example/radiographs/max-carpus-lateral-20240822.jpg', 
   'max-carpus-lateral-20240822.jpg',
   (SELECT id FROM app_user WHERE email = 'michael.chen@vettrack.local')),
  
  ('10000001-0000-0000-0000-000000000001', '20000001-0001-0000-0000-000000000002', NULL, 'image/jpeg',
   'https://storage.vettrack.example/radiographs/max-carpus-ap-20240822.jpg',
   'max-carpus-ap-20240822.jpg',
   (SELECT id FROM app_user WHERE email = 'michael.chen@vettrack.local')),
  
  -- Rocky's hip radiographs
  ('10000004-0000-0000-0000-000000000001', NULL, '50000003-0000-0000-0000-000000000001', 'image/jpeg',
   'https://storage.vettrack.example/radiographs/rocky-hips-vd-20241112.jpg',
   'rocky-hips-vd-ofa-20241112.jpg',
   (SELECT id FROM app_user WHERE email = 'michael.chen@vettrack.local')),
  
  -- Bella's skin condition photos
  ('10000002-0000-0000-0000-000000000001', '20000002-0001-0000-0000-000000000001', NULL, 'image/jpeg',
   'https://storage.vettrack.example/photos/bella-abdomen-20241110.jpg',
   'bella-dermatitis-abdomen.jpg',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  ('10000002-0000-0000-0000-000000000001', '20000002-0001-0000-0000-000000000001', NULL, 'image/jpeg',
   'https://storage.vettrack.example/photos/bella-paws-20241110.jpg',
   'bella-dermatitis-paws.jpg',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  -- Whiskers' glucose curve
  ('10000004-0000-0000-0000-000000000002', NULL, '50000004-0000-0000-0000-000000000001', 'application/pdf',
   'https://storage.vettrack.example/lab-results/whiskers-glucose-curve-20241108.pdf',
   'whiskers-glucose-curve-20241108.pdf',
   (SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local')),
  
  -- Charlie's ear cytology
  ('10000002-0000-0000-0000-000000000002', '20000002-0002-0000-0000-000000000001', NULL, 'image/jpeg',
   'https://storage.vettrack.example/microscopy/charlie-ear-cytology-20241105.jpg',
   'charlie-ear-cytology-yeast.jpg',
   (SELECT id FROM app_user WHERE email = 'james.wilson@vettrack.local'))
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Audit Log (Sample entries)
-- =====================================================================
INSERT INTO audit_log (actor_user_id, entity_type, entity_id, action, diff_snapshot, occurred_at, ip) VALUES
  -- Patient creation
  ((SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local'), 
   'patient', '10000001-0000-0000-0000-000000000001', 'create',
   '{"name": "Max", "species": "Canine", "breed": "Golden Retriever"}'::jsonb,
   '2019-03-15 14:30:00+00', '192.168.1.100'),
  
  -- Visit creation
  ((SELECT id FROM app_user WHERE email = 'sarah.johnson@vettrack.local'),
   'visit', '20000001-0001-0000-0000-000000000001', 'create',
   '{"patient_id": "10000001-0000-0000-0000-000000000001", "reason": "Annual wellness exam"}'::jsonb,
   '2024-11-15 10:30:00+00', '192.168.1.100'),
  
  -- Medication update
  ((SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local'),
   'medication', '30000003-0000-0000-0000-000000000001', 'update',
   '{"old": {"next_due_at": "2024-11-18 08:00:00"}, "new": {"next_due_at": "2024-11-19 08:00:00"}}'::jsonb,
   '2024-11-18 08:05:00+00', '192.168.1.105'),
  
  -- Reminder status change
  ((SELECT id FROM app_user WHERE email = 'emily.rodriguez@vettrack.local'),
   'reminder', '40000011-0000-0000-0000-000000000001', 'update',
   '{"old": {"status": "pending"}, "new": {"status": "done"}}'::jsonb,
   '2024-11-10 11:30:00+00', '192.168.1.105'),
  
  -- Exam finalized
  ((SELECT id FROM app_user WHERE email = 'michael.chen@vettrack.local'),
   'exam', '50000003-0000-0000-0000-000000000001', 'update',
   '{"old": {"status": "draft"}, "new": {"status": "final"}}'::jsonb,
   '2024-11-12 13:45:00+00', '192.168.1.102')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- Additional exam templates for variety
-- =====================================================================
INSERT INTO exam_template (name, version, description, is_active, fields_json) VALUES
  (
    'Dermatology Consultation',
    1,
    'Comprehensive dermatological examination',
    TRUE,
    '[
      {"key": "chief_complaint", "label": "Chief Complaint", "type": "textarea", "required": true},
      {"key": "duration", "label": "Duration of Signs", "type": "text", "required": true},
      {"key": "pruritus_score", "label": "Pruritus Score (0-10)", "type": "number", "required": true},
      {"key": "distribution", "label": "Lesion Distribution", "type": "textarea", "required": true},
      {"key": "primary_lesions", "label": "Primary Lesions", "type": "select", "options": ["Macule", "Papule", "Nodule", "Wheal", "Vesicle", "Pustule", "Cyst"], "required": false},
      {"key": "secondary_lesions", "label": "Secondary Lesions", "type": "select", "options": ["Scale", "Crust", "Erosion", "Ulcer", "Excoriation", "Hyperpigmentation", "Lichenification"], "required": false},
      {"key": "coat_quality", "label": "Coat Quality", "type": "textarea", "required": false},
      {"key": "skin_scraping", "label": "Skin Scraping Results", "type": "textarea", "required": false},
      {"key": "tape_cytology", "label": "Tape Cytology Results", "type": "textarea", "required": false},
      {"key": "woods_lamp", "label": "Woods Lamp Examination", "type": "select", "options": ["Not performed", "Negative", "Positive"], "required": false},
      {"key": "diagnosis", "label": "Diagnosis", "type": "textarea", "required": true},
      {"key": "treatment_plan", "label": "Treatment Plan", "type": "textarea", "required": true}
    ]'::jsonb
  ),
  (
    'Orthopedic Evaluation',
    1,
    'Musculoskeletal and joint assessment',
    TRUE,
    '[
      {"key": "limb_affected", "label": "Limb(s) Affected", "type": "text", "required": true},
      {"key": "lameness_grade", "label": "Lameness Grade (1-5)", "type": "number", "required": true},
      {"key": "weight_bearing", "label": "Weight Bearing", "type": "select", "options": ["Full", "Partial", "Non-weight bearing"], "required": true},
      {"key": "range_of_motion", "label": "Range of Motion", "type": "textarea", "required": true},
      {"key": "joint_effusion", "label": "Joint Effusion", "type": "select", "options": ["None", "Mild", "Moderate", "Severe"], "required": false},
      {"key": "crepitus", "label": "Crepitus Present", "type": "select", "options": ["No", "Yes"], "required": false},
      {"key": "pain_response", "label": "Pain Response", "type": "textarea", "required": true},
      {"key": "muscle_atrophy", "label": "Muscle Atrophy", "type": "select", "options": ["None", "Mild", "Moderate", "Severe"], "required": false},
      {"key": "drawer_sign", "label": "Drawer Sign", "type": "select", "options": ["Negative", "Positive"], "required": false},
      {"key": "tibial_thrust", "label": "Tibial Thrust", "type": "select", "options": ["Negative", "Positive"], "required": false},
      {"key": "imaging_findings", "label": "Imaging Findings", "type": "textarea", "required": false},
      {"key": "diagnosis", "label": "Diagnosis", "type": "textarea", "required": true},
      {"key": "treatment_plan", "label": "Treatment Plan", "type": "textarea", "required": true}
    ]'::jsonb
  )
ON CONFLICT (name, version) DO NOTHING;

-- =====================================================================
-- End of Development Seed
-- =====================================================================

-- Display summary
DO $$
DECLARE
  user_count INT;
  owner_count INT;
  patient_count INT;
  visit_count INT;
  medication_count INT;
  reminder_count INT;
  exam_count INT;
  template_count INT;
BEGIN
  SELECT COUNT(*) INTO user_count FROM app_user;
  SELECT COUNT(*) INTO owner_count FROM owner;
  SELECT COUNT(*) INTO patient_count FROM patient;
  SELECT COUNT(*) INTO visit_count FROM visit;
  SELECT COUNT(*) INTO medication_count FROM medication;
  SELECT COUNT(*) INTO reminder_count FROM reminder;
  SELECT COUNT(*) INTO exam_count FROM exam;
  SELECT COUNT(*) INTO template_count FROM exam_template;
  
  RAISE NOTICE '=============================================================';
  RAISE NOTICE 'VetTrack Development Seed Data Summary';
  RAISE NOTICE '=============================================================';
  RAISE NOTICE 'Users:         %', user_count;
  RAISE NOTICE 'Owners:        %', owner_count;
  RAISE NOTICE 'Patients:      %', patient_count;
  RAISE NOTICE 'Visits:        %', visit_count;
  RAISE NOTICE 'Medications:   %', medication_count;
  RAISE NOTICE 'Reminders:     %', reminder_count;
  RAISE NOTICE 'Exams:         %', exam_count;
  RAISE NOTICE 'Templates:     %', template_count;
  RAISE NOTICE '=============================================================';
  RAISE NOTICE 'All development seed data loaded successfully!';
  RAISE NOTICE '=============================================================';
END $$;
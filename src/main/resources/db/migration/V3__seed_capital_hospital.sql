-- ─────────────────────────────────────────────
-- HOSPITAL
-- ─────────────────────────────────────────────
INSERT INTO hospitals (id, name, address, phone, plan_type, is_active)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'Capital Hospital',
    'New Delhi',
    '9999999999',
    'PRO',
    true
);

-- ─────────────────────────────────────────────
-- SUBSCRIPTION (PRO, no limits)
-- ─────────────────────────────────────────────
INSERT INTO subscriptions (id, hospital_id, plan_type, status, max_departments, max_doctors, max_staff_users)
VALUES (
    'a0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000001',
    'PRO',
    'ACTIVE',
    10,
    20,
    20
);

-- ─────────────────────────────────────────────
-- DEPARTMENTS
-- ─────────────────────────────────────────────
INSERT INTO departments (id, hospital_id, name, is_active) VALUES
    ('d0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'Cardiology',    true),
    ('d0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', 'Oncology',      true),
    ('d0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000001', 'Gynecology',    true),
    ('d0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000001', 'Dermatology',   true);

-- ─────────────────────────────────────────────
-- DOCTORS
-- ─────────────────────────────────────────────
INSERT INTO doctors (id, hospital_id, department_id, name, specialization, is_available, can_manage_queue) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'Dr. Naresh Trehan',    'Cardiology',   true, true),
    ('e0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000002', 'Dr. Kuljinder Sodhi',  'Oncology',     true, true),
    ('e0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000003', 'Dr. Manmeet Sodhi',    'Gynecology',   true, true),
    ('e0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000004', 'Dr. Ketki',            'Dermatology',  true, true);

-- ─────────────────────────────────────────────
-- STAFF USERS  (password: admin123)
-- ─────────────────────────────────────────────
INSERT INTO staff_users (id, hospital_id, doctor_id, name, email, password_hash, role, is_active) VALUES
    -- Hospital Admin
    ('f0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', NULL,
     'Admin', 'admin@baari.com',
     '$2a$10$oA/ylAbzES6ncs45vPFN/O/zkWjWlstcu8vZTE/ChcwbwG5I5j1D6',
     'HOSPITAL_ADMIN', true),

    -- Doctors
    ('f0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001',
     'Dr. Naresh Trehan', 'dr.trehan@baari.com',
     '$2a$10$oA/ylAbzES6ncs45vPFN/O/zkWjWlstcu8vZTE/ChcwbwG5I5j1D6',
     'DOCTOR', true),

    ('f0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000002',
     'Dr. Kuljinder Sodhi', 'dr.kuljinder@baari.com',
     '$2a$10$oA/ylAbzES6ncs45vPFN/O/zkWjWlstcu8vZTE/ChcwbwG5I5j1D6',
     'DOCTOR', true),

    ('f0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000003',
     'Dr. Manmeet Sodhi', 'dr.manmeet@baari.com',
     '$2a$10$oA/ylAbzES6ncs45vPFN/O/zkWjWlstcu8vZTE/ChcwbwG5I5j1D6',
     'DOCTOR', true),

    ('f0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000004',
     'Dr. Ketki', 'dr.ketki@baari.com',
     '$2a$10$oA/ylAbzES6ncs45vPFN/O/zkWjWlstcu8vZTE/ChcwbwG5I5j1D6',
     'DOCTOR', true),

    -- Receptionist
    ('f0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000001', NULL,
     'Priya', 'priya@baari.com',
     '$2a$10$oA/ylAbzES6ncs45vPFN/O/zkWjWlstcu8vZTE/ChcwbwG5I5j1D6',
     'RECEPTIONIST', true);

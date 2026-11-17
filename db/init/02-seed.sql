INSERT INTO policies
  (policy_number, holder_name, holder_email, holder_phone, status, expiry_date, premium_amount, updated_at)
VALUES
  ('POL-5001', 'Asha Rao',    'asha@example.com',  '9000000001', 'ACTIVE', CURRENT_DATE + 20,  1250.00, now()),
  ('POL-5002', 'Ravi Kumar',  'ravi@example.com',  '9000000002', 'ACTIVE', CURRENT_DATE + 10,  3400.00, now()),
  ('POL-5003', 'Meena Iyer',  'meena@example.com', '9000000003', 'ACTIVE', CURRENT_DATE + 200,  980.00, now());

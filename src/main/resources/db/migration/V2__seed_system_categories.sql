-- SpendWise system category seed data.
-- System categories have user_id = NULL and is_system = TRUE.
-- Food uses the UUID referenced by the expenses.category_id column default in V1.

INSERT INTO public.categories (id, user_id, name, icon, is_system)
VALUES
    ('00000000-0000-0000-0000-000000000001', NULL, 'Food', NULL, TRUE),
    ('00000000-0000-0000-0000-000000000002', NULL, 'Transport', NULL, TRUE),
    ('00000000-0000-0000-0000-000000000003', NULL, 'Shopping', NULL, TRUE),
    ('00000000-0000-0000-0000-000000000004', NULL, 'Bills & Utilities', NULL, TRUE),
    ('00000000-0000-0000-0000-000000000005', NULL, 'Healthcare', NULL, TRUE),
    ('00000000-0000-0000-0000-000000000006', NULL, 'Entertainment', NULL, TRUE),
    ('00000000-0000-0000-0000-000000000007', NULL, 'Education', NULL, TRUE),
    ('00000000-0000-0000-0000-000000000008', NULL, 'Travel', NULL, TRUE),
    ('00000000-0000-0000-0000-000000000009', NULL, 'Personal Care', NULL, TRUE),
    ('00000000-0000-0000-0000-000000000010', NULL, 'Other', NULL, TRUE);

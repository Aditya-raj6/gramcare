-- Drops and recreates all GramCare databases, so the next start re-seeds the
-- demo data. DESTRUCTIVE: stop all services first.
--   psql -U postgres -f database/99-reset-demo-data.sql
DROP DATABASE IF EXISTS gramcare_resident;
DROP DATABASE IF EXISTS gramcare_appointment;
DROP DATABASE IF EXISTS gramcare_inventory;
DROP DATABASE IF EXISTS gramcare_sanitation;
DROP DATABASE IF EXISTS gramcare_action;
\i 01-create-databases.sql

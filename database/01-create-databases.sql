-- GramCare: one PostgreSQL database per microservice (database-per-service).
-- Run once as a superuser, e.g.:
--   psql -U postgres -f database/01-create-databases.sql
-- Tables are created automatically by each service on first start
-- (spring.jpa.hibernate.ddl-auto=update), and demo data is inserted only when
-- the tables are empty. The 02-*.sql files document the resulting schema and
-- can be used instead of auto-creation if you prefer (see README).

CREATE DATABASE gramcare_resident;
CREATE DATABASE gramcare_appointment;
CREATE DATABASE gramcare_inventory;
CREATE DATABASE gramcare_sanitation;
CREATE DATABASE gramcare_action;

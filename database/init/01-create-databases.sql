-- HealthHarbor: one database per microservice (database-per-service pattern).
-- Executed automatically by the postgres container on first start, or manually:
--   psql -U postgres -f database/init/01-create-databases.sql

DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'healthharbor') THEN
      CREATE ROLE healthharbor LOGIN PASSWORD 'healthharbor';
   END IF;
END
$$;

SELECT 'CREATE DATABASE hh_auth OWNER healthharbor'         WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hh_auth')\gexec
SELECT 'CREATE DATABASE hh_patient OWNER healthharbor'      WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hh_patient')\gexec
SELECT 'CREATE DATABASE hh_healthcenter OWNER healthharbor' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hh_healthcenter')\gexec
SELECT 'CREATE DATABASE hh_appointment OWNER healthharbor'  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hh_appointment')\gexec
SELECT 'CREATE DATABASE hh_inventory OWNER healthharbor'    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hh_inventory')\gexec
SELECT 'CREATE DATABASE hh_healthcamp OWNER healthharbor'   WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hh_healthcamp')\gexec
SELECT 'CREATE DATABASE hh_sanitation OWNER healthharbor'   WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hh_sanitation')\gexec
SELECT 'CREATE DATABASE hh_water OWNER healthharbor'        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hh_water')\gexec
SELECT 'CREATE DATABASE hh_dashboard OWNER healthharbor'    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hh_dashboard')\gexec

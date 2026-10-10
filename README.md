# HealthHarbor
**Team17 · Integrated Rural Health & Sanitation Management Platform**

HealthHarbor is an enterprise web application for primary-healthcare, sanitation and public-health staff in rural areas. It brings resident records, health centres, appointments, medicine stock, health camps, sanitation facilities and water-quality monitoring into one system. Authorised officers get a single view of every pending action.

Stack: **Java 21 · Spring Boot 3.4 · Spring Cloud (Eureka, Gateway, OpenFeign) · PostgreSQL 16 · React 18 + Ant Design**

---

## 1. Requirements → implementation

| # | Requirement | Where it lives |
|---|-------------|----------------|
| 1 | Resident & Patient Management | `patient-service`: residents, medical records, visit history · UI **Residents & Patients** (profile drawer with visits, records and appointments) |
| 2 | Health Centre Management | `healthcenter-service`: centres, facilities, workers, services, operating schedules / duty roster · UI **Health Centres** |
| 3 | Appointment & Consultation Management | `appointment-service`: booking with double-booking checks, a workflow, consultations with vitals and a treatment plan. A follow-up appointment is booked automatically and a visit is written to the patient's history over Feign |
| 4 | Medicine & Inventory Management | `inventory-service`: catalogue, stock per centre and batch, issue/receive ledger, expiry write-off. Requests are raised automatically when stock runs low, and fulfilment adds the stock back |
| 5 | Health Camp Management | `healthcamp-service`: camps, staff, beneficiaries, screenings. A follow-up is created automatically when a screening is abnormal and the person is referred |
| 6 | Sanitation Facility Management | `sanitation-service`: facilities, cleanliness inspections (a poor rating raises a cleaning task), maintenance work orders, complaints that convert to work orders |
| 7 | Water & Public Health Monitoring | `water-service`: water sources, quality tests checked against **BIS 10500:2012** (an unsafe result marks the source contaminated and opens an issue), public-health issues, corrective actions |
| 8 | Community Dashboard & Reporting | `dashboard-service`: KPIs gathered in parallel from all services, a consolidated **Pending Actions** list sorted by priority, a village-wise report, charts and CSV export |
| 9 | Enterprise integration | 9 domain microservices plus the Eureka registry and an API gateway, REST APIs with OpenAPI docs, one PostgreSQL database per service, JWT security with role-based access |

### The illustrative example, end to end
> *A health worker reports that a village sanitation facility needs maintenance and that a nearby health centre is short of medicine.*

1. The health worker (`worker / worker123`) opens **Field Reports → New field report** and adds two issues to one report.
2. `dashboard-service` saves the report (`FR-00001`). It routes each issue through OpenFeign:
   * the facility issue goes to `sanitation-service` and becomes **maintenance task MNT-…**
   * the medicine shortage goes to `inventory-service` and becomes **replenishment request REQ-…**
3. Each team updates its record in its own module (sanitation: ASSIGNED → IN_PROGRESS → COMPLETED; pharmacist or officer: APPROVED → FULFILLED, which adds the stock automatically).
4. The officer (`admin / admin123`) sees both items in **Pending Actions**, sorted by priority across all modules. The officer can update either one from there. Statuses are synced live back onto the field report.

---

## 2. Architecture

```
                         ┌──────────────────────────┐
  Browser (React SPA) ──►│ API Gateway :8080        │  JWT validation, role-based access,
                         │ Spring Cloud Gateway     │  CORS, routing via Eureka (lb://)
                         └────────────┬─────────────┘
                                      │
      ┌─────────────┬─────────────┬───┴─────────┬──────────────┬──────────────┐
      ▼             ▼             ▼             ▼              ▼              ▼
  auth :8081   patient :8082  healthcenter  appointment   inventory :8085  healthcamp :8086
                              :8083         :8084 ──Feign──► patient / healthcenter
      ▼             ▼             ▼
 sanitation   water :8088    dashboard :8089 ──Feign──► all services (stats, pending, routing)
   :8087
      │  every service registers with ► Eureka discovery-server :8761
      ▼
  PostgreSQL :5432  — one database per service: hh_auth, hh_patient, hh_healthcenter,
                      hh_appointment, hh_inventory, hh_healthcamp, hh_sanitation, hh_water, hh_dashboard
```

The design is covered in detail in [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md): service catalogue, data model, workflows, security and design decisions.

### Repository layout
```
pom.xml                    Maven parent (Spring Boot 3.4.5, Spring Cloud 2024.0.1)
hh-common/                 Shared library: BaseEntity, generic CRUD layer, error handling, DTOs
discovery-server/          Eureka registry
api-gateway/               Spring Cloud Gateway + JWT filter + AccessPolicy
auth-service/              Users, roles, login (JWT issuance, BCrypt)
patient-service/           Residents, medical records, visits
healthcenter-service/      Centres, facilities, workers, services, schedules
appointment-service/       Appointments, consultations, treatment plans (Feign → patient, healthcenter)
inventory-service/         Medicines, stock, transactions, replenishment
healthcamp-service/        Camps, staff, beneficiaries, screenings, follow-ups
sanitation-service/        Facilities, inspections, maintenance, complaints
water-service/             Water sources, quality tests, public-health issues, corrective actions
dashboard-service/         Dashboard aggregation, pending actions, reports, field-report routing
frontend/                  React 18 + Vite + Ant Design + Recharts single-page app
database/init/             SQL that creates the role and all 9 databases
database/schema/           Generated DDL of every database (reference)
scripts/                   start-all.sh / stop-all.sh / gen-crud.sh
Dockerfile, docker-compose.yml
```

---

## 3. Running the project

### Option A: Docker Compose (everything in containers)
```bash
docker compose up --build
```
Then open **http://localhost:3000**. The compose file starts PostgreSQL (and creates the 9 databases from `database/init`), Eureka, the gateway, the 9 services and the nginx-served UI. On first start, wait about 1–2 minutes for every service to register with Eureka.

### Option B: Local JVMs (for development)
Prerequisites: JDK 21 or newer (tested on 21; built to also run on 25), Node 18+, PostgreSQL 16 (optional, see below).
Maven does **not** need to be installed: use the bundled wrapper (`./mvnw` on Mac/Linux, `.\mvnw.cmd` on Windows).

**Windows (PowerShell):**
```powershell
.\mvnw.cmd -DskipTests package
.\scripts\start-all.ps1 h2            # omit "h2" to use PostgreSQL; stop with .\scripts\stop-all.ps1
cd frontend; npm install; npm run dev   # http://localhost:5173
```
If PowerShell blocks the script, run `Set-ExecutionPolicy -Scope Process Bypass` first.

**Mac / Linux:**

```bash
# 1. Databases (creates role healthharbor/healthharbor and hh_* databases)
psql -U postgres -f database/init/01-create-databases.sql

# 2. Build and start the backend (Eureka first, then the services and the gateway)
./mvnw -DskipTests package
scripts/start-all.sh                 # logs in ./logs, stop with scripts/stop-all.sh

# 3. Frontend (Vite proxies /api to the gateway on :8080)
cd frontend && npm install && npm run dev      # http://localhost:5173
```

**No PostgreSQL?** Run `scripts/start-all.sh h2`. Every service then uses an embedded H2 file database (in `<service>/data/`) running in PostgreSQL compatibility mode.

Tables are created by Hibernate on first start. Every service **seeds realistic demo data** for six villages (14 residents, 4 centres, 12 staff, 25 stock batches, 4 camps, 8 sanitation facilities, 7 water sources and more). Set `SEED_DATA=false` to disable seeding.

### Demo accounts
| Role | Username | Password | Can write to |
|------|----------|----------|--------------|
| District Health Officer (Admin) | `admin` | `admin123` | everything, including users and health centres |
| Doctor | `doctor` | `doctor123` | patients, appointments, consultations, camps, inventory |
| ASHA / Health Worker | `worker` | `worker123` | patients, appointments, camps, inventory requests, sanitation, water, field reports |
| Pharmacist | `pharmacist` | `pharma123` | medicine catalogue, stock, replenishment |
| Sanitation Officer | `sanitation` | `sanit123` | sanitation and water modules |

Every role can read every module.

### Useful URLs (local)
| What | URL |
|------|-----|
| Web app | http://localhost:5173 (dev) / http://localhost:3000 (docker) |
| API gateway | http://localhost:8080/api/… |
| Eureka dashboard | http://localhost:8761 |
| Swagger UI per service | http://localhost:808X/swagger-ui.html (e.g. 8082 for patient-service) |

---

## 4. REST API overview (through the gateway)

All endpoints need `Authorization: Bearer <token>` except `POST /api/auth/login`. Every CRUD resource supports:
`GET /resource` (any entity field works as a filter, e.g. `/api/stock?healthCentreId=2`), `GET /resource/{id}`, `POST`, `PUT /{id}`, `PATCH /{id}/status` and `DELETE /{id}`.

| Service | Resources | Extra endpoints |
|---------|-----------|-----------------|
| auth | `/api/auth/login`, `/api/auth/me`, `/api/users` | `/api/auth/register`, `/api/auth/change-password` |
| patient | `/api/residents` (`?q=` search), `/api/medical-records`, `/api/visits` | `/api/residents/stats` |
| healthcenter | `/api/health-centres`, `/api/facilities`, `/api/health-workers`, `/api/centre-services`, `/api/schedules` | `/api/health-centres/stats`, `/pending` |
| appointment | `/api/appointments`, `/api/consultations` | `/api/appointments/stats`, `/pending` |
| inventory | `/api/medicines`, `/api/stock`, `/api/stock-transactions`, `/api/replenishments` | `/api/stock/{id}/issue`, `/receive`, `/write-off`, `/api/stock/attention`, `/api/inventory/stats`, `/pending` |
| healthcamp | `/api/health-camps`, `/api/camp-staff`, `/api/beneficiaries`, `/api/screenings`, `/api/follow-ups` | `/api/health-camps/{id}/summary`, `/stats`, `/pending` |
| sanitation | `/api/sanitation/facilities`, `/inspections`, `/maintenance`, `/complaints` | `/api/sanitation/complaints/{id}/convert-to-task`, `/stats`, `/pending` |
| water | `/api/water/sources`, `/tests`, `/issues`, `/actions` | `/api/water/stats`, `/pending` |
| dashboard | `/api/field-reports` | `/api/field-reports/{id}/retry`, `/api/dashboard/summary`, `/api/dashboard/pending-actions`, `/api/dashboard/reports/villages` |

Example:
```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"worker","password":"worker123"}' | jq -r .token)

curl -s -X POST localhost:8080/api/field-reports -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{
  "village": "Sundarpur",
  "items": [
    {"itemType":"SANITATION_MAINTENANCE","facilityId":2,"category":"PLUMBING","description":"Flush tanks broken","priority":"HIGH"},
    {"itemType":"MEDICINE_SHORTAGE","medicineId":1,"healthCentreId":2,"healthCentreName":"Sub-Centre Sundarpur","quantity":300,"description":"Paracetamol running out","priority":"CRITICAL"}
  ]}'
```

---

## 5. Tests
```bash
mvn test
```
* `WaterQualityEvaluatorTest`: BIS 10500 classification (safe / marginal / unsafe).
* `AccessPolicyTest`: the gateway's role-based write rules.
* `StockWorkflowIntegrationTest`: issue below reorder level → automatic replenishment → no duplicate request → approval → fulfilment restocks the batch (Spring Boot, in-memory H2).

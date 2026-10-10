# GramCare — Integrated Rural Health & Sanitation Management

**One Village. One Connected Health & Sanitation System.**

GramCare helps rural health workers, health-centre administrators, inventory managers, sanitation officers and authorized officers manage healthcare and sanitation operations — and tracks every reported problem (a broken public toilet, a medicine shortage, bad water) through one **Unified Action Centre** from report to closure.

Built for the Enterprise Java Hackathon with the approved stack only: **Java 21, Spring Boot 3.3, Spring MVC/REST, Spring Data JPA, PostgreSQL, Thymeleaf, HTML/CSS/vanilla JS, Maven**.

---

## 1. Build status — read this first

The code was written and checked in an environment **without access to Maven Central**, so it has **not yet been built with Maven or run end-to-end**. What *was* verified:

| Check | Result |
|---|---|
| Every Java file in all 6 services (main + tests) type-checked with `javac` against hand-written stubs of the Spring/JPA APIs used | 0 errors |
| All SQL scripts in `database/` executed against a real PostgreSQL 16 server | all succeeded |
| `mvn test`, application start-up, Thymeleaf rendering, live REST calls | **not yet run** |

So the first `mvn clean package` on your machine is the real compile. If anything fails, the troubleshooting section (§9) lists the most likely causes and fixes.

---

## 2. Architecture

Six independently runnable Spring Boot applications, each with its own Maven `pom.xml`, port, application name and PostgreSQL database. Services talk only via REST — no service reads another's database.

```
                         ┌──────────────────────────┐
  Browser ──────────────▶│  web-app (8080)          │  Thymeleaf UI, Spring MVC
                         └────┬────┬────┬────┬──────┘
                REST          │    │    │    │
     ┌────────────────────────┘    │    │    └────────────────────────┐
     ▼                             ▼    ▼                             ▼
┌───────────────┐  ┌─────────────────┐  ┌──────────────────┐  ┌───────────────────┐
│ resident-     │  │ appointment-    │  │ inventory-       │  │ sanitation-       │
│ service 8081  │  │ camp-service    │  │ service 8083     │  │ service 8084      │
│ residents,    │  │ 8082            │  │ medicines,       │  │ facilities,       │
│ health centres│  │ appointments,   │  │ batches, stock,  │  │ complaints, water │
│ workers       │  │ camps           │  │ replenishment    │  │ quality           │
└──────┬────────┘  └───────┬─────────┘  └───────┬──────────┘  └────────┬──────────┘
       │                   │      POST /api/actions (idempotent)       │
       │                   │                    └──────────┬───────────┘
       │   GET counts      │   GET counts                  ▼
       └───────────────────┴──────────────────▶ ┌──────────────────────────┐
                                                │ action-service (8085)    │
                                                │ unified action register, │
                                                │ dashboard aggregation    │
                                                └──────────────────────────┘
   Each service ──▶ its own PostgreSQL database (gramcare_resident, gramcare_appointment,
                    gramcare_inventory, gramcare_sanitation, gramcare_action)
```

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the data model, the action-tracking workflow and design decisions.

| Service | Port | Database | Responsibility |
|---|---|---|---|
| `web-app` | 8080 | — | Thymeleaf UI, demo login, calls all services |
| `resident-service` | 8081 | `gramcare_resident` | Residents, visit history, health centres, workers, services, schedules |
| `appointment-camp-service` | 8082 | `gramcare_appointment` | Appointments, consultations, treatments, health camps, staff, beneficiaries, screenings, follow-ups |
| `inventory-service` | 8083 | `gramcare_inventory` | Medicines, batches, stock in/issue, low-stock detection, replenishment requests |
| `sanitation-service` | 8084 | `gramcare_sanitation` | Facilities, inspections, complaints, water-quality observations, corrective actions |
| `action-service` | 8085 | `gramcare_action` | Unified action register, assignment, status history, dashboard summary |

---

## 3. Prerequisites

* **JDK 21** (`java -version`)
* **Maven 3.9+** (`mvn -version`) — or use the Maven bundled inside Spring Tools for Eclipse
* **PostgreSQL 14+** running on `localhost:5432` (tested scripts on 16)
* Internet access on first build (Maven downloads Spring Boot dependencies)

---

## 4. Database setup

1. Create the five databases (once):
   ```bash
   psql -U postgres -f database/01-create-databases.sql
   ```
2. That's all that is required. On first start each service creates its tables (`spring.jpa.hibernate.ddl-auto=update`) and inserts **fictional demo data only if its tables are empty** — restarting never duplicates data.
3. *(Optional)* `database/02-…` to `06-…-schema.sql` contain the equivalent DDL with constraints and indexes, if you prefer to create tables yourself. They are also the data-model documentation.
4. To start over with fresh demo data: stop all services, then `psql -U postgres -f database/99-reset-demo-data.sql` (run from inside the `database/` folder — it drops all GramCare databases).

### Credentials

The `application.properties` files contain **placeholders**, not real secrets:

```properties
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
```

If your PostgreSQL password is not `postgres`, set an environment variable instead of editing the files:

* Windows (cmd): `set DB_PASSWORD=yourpassword` (in each terminal), or set it once in *System → Environment Variables*
* PowerShell: `$env:DB_PASSWORD="yourpassword"`
* STS: *Run Configurations → (service) → Environment → Add* `DB_PASSWORD`

`DB_HOST`, `DB_PORT` and `DB_USERNAME` work the same way.

---

## 5. Running

### Start order

Any order works — services degrade gracefully when a neighbour is down, and unlinked records are reconciled automatically every 60 s. For the smoothest demo:

1. `action-service` (8085)
2. `resident-service` (8081)
3. `inventory-service` (8083)
4. `sanitation-service` (8084)
5. `appointment-camp-service` (8082)
6. `web-app` (8080)

### Build first (recommended)

On Windows, run `build-all.bat` from the repository root. It builds all six services in order, runs their tests, and stops at the first failure with the error on screen. Use `build-all.bat skiptests` for a faster build. Elsewhere: `mvn clean package` inside each service folder.

### Option A — terminals

In six terminals, from the repository root:

```bash
cd action-service && mvn spring-boot:run
cd resident-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd sanitation-service && mvn spring-boot:run
cd appointment-camp-service && mvn spring-boot:run
cd web-app && mvn spring-boot:run
```

On Windows, `start-all.bat` opens all six in separate windows.

### Option B — Spring Tools for Eclipse (STS)

1. *File → Import → Maven → Existing Maven Projects*, choose the repository root; all six projects are detected (each has its own `pom.xml`).
2. Wait for dependencies to download (bottom-right progress bar).
3. In the *Boot Dashboard*, select all six apps and press *(Re)start*, or right-click each `*Application.java` → *Run As → Spring Boot App*.

### Verify

Each service exposes `GET /api/health`:

```
http://localhost:8081/api/health   ... http://localhost:8085/api/health   http://localhost:8080/api/health
```

Then open **http://localhost:8080**, pick a demo role and continue.

### Run the tests

```bash
cd inventory-service && mvn test        # repeat per service
```

Tests use an in-memory H2 database (profile `test`), so PostgreSQL does not need to be running. Business rules covered: resident creation and validation, appointment creation and past-date rejection, stock deduction, negative-stock prevention, earliest-expiry-first issuing, low-stock detection and automatic replenishment, sanitation complaint creation when the action service is down, water-quality flagging, action idempotency, assignment, status history, default due dates, and dashboard summary calculation with unavailable services.

---

## 6. Demo walkthrough (the core workflow, ~5 minutes)

1. **Log in** as *Health Worker*. The dashboard already shows seeded data: pending actions, high-priority issues, issues by category and by village.
2. **Report a sanitation problem:** *Dashboard → Report Sanitation Issue*. Facility *Kondapur Bus Stand Public Toilet*, title "Public toilet requires maintenance", priority High → Submit. The complaint is saved in **sanitation-service**, which immediately calls **action-service** to create a linked action item, routed to the *Sanitation Department* with a due date set from priority.
3. **Report a medicine shortage:** *Inventory → ORS Sachet* (already below minimum) → *Request replenishment*, village *Anandpur* → Submit. Saved in **inventory-service**, linked action item created in category *MEDICINE_STOCK*, routed to *Pharmacy & Inventory*.
   *Alternative (the full clinical path):* *Appointments* → open today's appointment → record a consultation → *Add treatment* and choose **Dispense from stock** (e.g. 30 × Amoxicillin). The stock is issued from inventory-service (earliest-expiry batch first). If that pushes the medicine below its minimum, a replenishment request **and** a MEDICINE_STOCK action item are raised automatically. Asking for more than is in stock is refused with a clear message, and nothing is saved.
4. **Log out → log in as Administrator.** Open the **Action Centre**: both issues are listed with priority, department, due date. Filter by category/village/priority/status/assignee/overdue.
5. **Assign** each item to a responsible person (status OPEN → ASSIGNED), then **update status** to IN_PROGRESS and finally RESOLVED with resolution notes. Each change is recorded in the item's history with who/when/notes.
6. Back on the **Dashboard**, the pending count and charts reflect the changes. Refresh the browser — everything persists in PostgreSQL.
7. *Resilience demo (optional):* stop `action-service`, report another complaint — it is saved and clearly marked **"Not linked"** with a *Retry link* button (no false success). Restart `action-service`; within a minute the reconciler links it, or press *Retry*. Thanks to idempotency, retries never create duplicates.

Other pages to show: residents (search, profile, visit history), health centres (workers, services, schedules), appointments (today/upcoming, consultation → treatment), health camps (staff, beneficiaries, screenings, follow-ups), water quality (measurements vs. reference limits, corrective actions), reports (date/category filters, printable).

### Demo roles

| Role | Intended use |
|---|---|
| Health Worker | Register residents, record visits, report issues |
| Inventory Manager | Medicines, stock, replenishment |
| Sanitation Officer | Facilities, complaints, water quality |
| Administrator | Dashboard, Action Centre, reports |

> **Security note:** demo mode only labels the session with the chosen role (shown in the top bar and recorded in action history). There is no password and no per-role access control — every role can open every page. A production deployment needs a real identity provider (e.g. Spring Security + OIDC), per-endpoint authorization, and authenticated service-to-service calls.

---

## 7. REST API catalogue

All request/response bodies are JSON. Errors use `{"status": 400, "message": "...", "timestamp": "..."}`; validation errors add `"errors": {field: message}`. Status codes: `201` on create, `400` invalid input, `404` missing record, `409` conflict, `500` unexpected. Stack traces and credentials are never returned. Every service also has `GET /api/health`.

### resident-service (8081)

| Method | Path | Description |
|---|---|---|
| GET | `/api/residents?name=&village=` | List / search residents |
| GET | `/api/residents/{id}` | Resident profile |
| POST | `/api/residents` | Register resident |
| PUT | `/api/residents/{id}` | Update resident |
| DELETE | `/api/residents/{id}` | Delete resident |
| GET / POST | `/api/residents/{id}/visits` | Visit history / add visit |
| GET | `/api/residents/count` | Count (dashboard) |
| GET | `/api/health-centres?name=&village=` | List / search centres |
| GET | `/api/health-centres/{id}` | Centre details |
| POST / PUT | `/api/health-centres`, `/api/health-centres/{id}` | Create / update centre |
| GET / POST | `/api/health-centres/{id}/workers` | Healthcare workers |
| GET / POST | `/api/health-centres/{id}/services` | Services offered |
| GET / POST | `/api/health-centres/{id}/schedules` | Operating schedule |
| GET | `/api/health-centres/count-active` | Count (dashboard) |

```json
POST /api/residents
{ "fullName": "Sarita Kamble", "village": "Anandpur", "gender": "FEMALE",
  "dateOfBirth": "1992-06-15", "contactPhone": "9800000000", "bloodGroup": "A+" }
```

### appointment-camp-service (8082)

| Method | Path | Description |
|---|---|---|
| GET | `/api/appointments?residentId=` | List appointments |
| GET | `/api/appointments/today`, `/api/appointments/upcoming` | Today / next 30 days |
| GET | `/api/appointments/{id}` | Appointment |
| POST | `/api/appointments` | Schedule (rejects past date/time) |
| PUT | `/api/appointments/{id}` | Update status `{"status":"COMPLETED"}` |
| GET / POST | `/api/appointments/{id}/consultations` | Consultations (adding one marks appointment COMPLETED) |
| GET / POST | `/api/consultations/{id}/treatments` | Treatments and follow-up date |
| GET / POST | `/api/health-camps` | List / create camps |
| GET | `/api/health-camps/active`, `/api/health-camps/{id}` | Active camps / camp |
| PUT | `/api/health-camps/{id}` | Update status |
| GET / POST | `/api/health-camps/{id}/staff`, `/api/health-camps/{id}/beneficiaries` | Staff / beneficiaries |
| GET / POST | `/api/beneficiaries/{id}/screenings`, `/api/beneficiaries/{id}/follow-ups` | Screenings / follow-ups |
| GET | `/api/beneficiaries/follow-ups/pending` | Pending follow-ups |

```json
POST /api/appointments
{ "residentId": 1, "healthCentreId": 1, "appointmentDateTime": "2026-10-12T10:30", "reason": "BP review" }
```

### inventory-service (8083)

| Method | Path | Description |
|---|---|---|
| GET | `/api/medicines?name=` | List with total usable stock and `lowStock` flag |
| GET | `/api/medicines/low-stock` | Medicines below minimum |
| GET / POST | `/api/medicines/{id}`, `/api/medicines` | Get / create medicine |
| GET | `/api/medicines/{id}/batches` | Batches with `expired` flag |
| POST | `/api/medicines/{id}/stock-in` | Receive a batch |
| POST | `/api/medicines/{id}/issue` | Issue stock (earliest expiry first; `400` if more than available) |
| GET | `/api/medicines/{id}/transactions` | Transaction history |
| GET | `/api/stock-transactions/recent` | Latest 20 transactions |
| GET / POST | `/api/replenishment-requests?status=` | List / create (creates linked MEDICINE_STOCK action) |
| PUT | `/api/replenishment-requests/{id}/status` | `{"status":"APPROVED"}` |
| POST | `/api/replenishment-requests/{id}/retry-action-link` | Retry linking to action-service |

```json
POST /api/medicines/3/issue          { "quantity": 20, "reason": "Dispensed at OPD", "performedBy": "Pharmacist" }
POST /api/replenishment-requests     { "medicineId": 3, "requestedQuantity": 200, "requestedBy": "Ramesh", "village": "Anandpur" }
```

### sanitation-service (8084)

| Method | Path | Description |
|---|---|---|
| GET / POST | `/api/sanitation/facilities` | List / register facilities |
| GET | `/api/sanitation/facilities/{id}` | Facility |
| GET / POST | `/api/sanitation/facilities/{id}/inspections` | Cleanliness inspections |
| GET / POST | `/api/sanitation/complaints?status=` | List / report complaint (creates linked SANITATION action) |
| GET | `/api/sanitation/complaints/{id}` | Complaint (`actionItemId` null = not yet linked) |
| PUT | `/api/sanitation/complaints/{id}/status` | `{"status","assignedWorker","closureNotes"}` |
| POST | `/api/sanitation/complaints/{id}/retry-action-link` | Retry linking |
| GET / POST | `/api/water-quality/observations?village=&reviewStatus=` | List / record (out-of-range creates WATER_QUALITY action) |
| GET | `/api/water-quality/observations/{id}` | Observation with values, limits used, `outOfRange` |
| PUT | `/api/water-quality/observations/{id}/review-status` | `{"reviewStatus":"CLEARED"}` |
| GET / POST | `/api/water-quality/observations/{id}/corrective-actions` | Corrective actions |
| POST | `/api/water-quality/observations/{id}/retry-action-link` | Retry linking |

```json
POST /api/sanitation/complaints
{ "facilityId": 1, "title": "Public toilet requires maintenance", "description": "Door latch broken",
  "reportedBy": "ASHA worker", "priority": "HIGH" }
POST /api/water-quality/observations
{ "village": "Anandpur", "waterSourceName": "Open well", "observationDate": "2026-10-10",
  "ph": 6.1, "turbidityNtu": 9.5, "residualChlorineMgL": 0.0, "recordedBy": "A. Deshmukh" }
```

### action-service (8085)

| Method | Path | Description |
|---|---|---|
| GET | `/api/actions?status=&category=&village=&priority=` | List (one filter at a time; the UI combines filters) |
| GET | `/api/actions/{id}` | Action item (includes `overdue`) |
| POST | `/api/actions` | Create — **idempotent** on `sourceService + sourceReferenceType + sourceReferenceId` |
| PUT | `/api/actions/{id}/assign` | `{"assignedDepartment","assignedPerson","changedBy"}` |
| PUT | `/api/actions/{id}/status` | `{"status":"RESOLVED","notes","changedBy"}` |
| GET | `/api/actions/{id}/history` | Status-change history |
| GET | `/api/actions/departments` | Category → department routing rules |
| GET | `/api/actions/summary` | Pending / critical counts |
| GET | `/api/dashboard/summary` | All dashboard figures (aggregated over REST, with per-service availability) |
| GET | `/api/dashboard/recent-activities?limit=10` | Latest action items |
| GET | `/api/dashboard/critical-actions` | CRITICAL items |

```json
POST /api/actions
{ "title": "Fever cluster in ward 3", "category": "GENERAL_HEALTH", "village": "Shivnagar",
  "priority": "HIGH", "reportedBy": "ASHA", "sourceService": "web-app",
  "sourceReferenceType": "MANUAL_REPORT", "sourceReferenceId": 1001 }
```

Enumerations — category: `MEDICINE_STOCK, SANITATION, WATER_QUALITY, HEALTH_CAMP, GENERAL_HEALTH`; priority: `LOW, MEDIUM, HIGH, CRITICAL`; status: `OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED`.

---

## 8. Project structure

```
gramcare/
├── web-app/                    Thymeleaf UI (controller, client, config; templates/, static/css, static/js)
├── resident-service/           each service: controller, service, repository, entity, dto, exception, config
├── appointment-camp-service/
├── inventory-service/
├── sanitation-service/
├── action-service/
├── database/                   create-databases, reference schema DDL, reset script
├── docs/ARCHITECTURE.md
├── build-all.bat               Windows: build + test all six services, stop on first failure
├── start-all.bat               Windows: start all six services
├── README.md
└── .gitignore
```

---

## 9. Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `FATAL: password authentication failed for user "postgres"` | Set `DB_PASSWORD` (§4) for that service's run configuration. |
| `FATAL: database "gramcare_xxx" does not exist` | Run `database/01-create-databases.sql`. |
| `Port 808x was already in use` | Another copy is running; stop it (STS Boot Dashboard → Stop) or change `server.port`. |
| Dashboard shows a yellow "figures unavailable" banner | That service isn't running; start it and refresh. Figures from it show as 0 rather than wrong numbers. |
| Complaint shows **Not linked** | `action-service` was down when it was saved. Start it; links are retried every 60 s, or press *Retry link*. |
| Thymeleaf error page `EL1008E Property ... cannot be found` | A template references a field the API doesn't return — check the service's JSON at `/api/...` and the field name in the template. |
| Maven can't resolve `spring-boot-starter-parent` | First build needs internet; behind a proxy configure `~/.m2/settings.xml`. In STS: right-click project → *Maven → Update Project (force)*. |
| `invalid target release: 21` | Install JDK 21 and point `JAVA_HOME` / STS *Installed JREs* at it. |
| Want fresh demo data | Stop services, run `database/99-reset-demo-data.sql` from inside `database/`, restart. |

---

## 10. Known limitations (honest list)

* **Not production-secured.** Demo role selection is not authentication or authorization; service-to-service calls are unauthenticated.
* **Synchronous REST between services.** No message broker (not in the approved stack). Consistency between a source record and its action item is *eventual*: the source record is committed first, and an unlinked record is visible as such and reconciled by a 60-second retry. Idempotent creation makes retries safe.
* **Status is not synced back.** Resolving an action item does not automatically close the originating sanitation complaint / replenishment request (and vice-versa); each is updated on its own page.
* **Cross-service ids are not validated** (e.g. an appointment's `residentId` is not checked against resident-service), by design of database-per-service; the UI only offers existing ids from dropdowns.
* Some read endpoints return JPA entities (with relationships hidden via `@JsonIgnore`) rather than dedicated DTOs; the main aggregates (residents, medicines, actions) use DTOs.
* Water-quality flags compare readings with configured reference limits (pH 6.5–8.5, turbidity ≤ 5 NTU, residual chlorine ≥ 0.2 mg/L). A flag means "needs review" — **not** a certification that water is safe or unsafe.
* All demo data is fictional; no real patient information is included.
* No Docker / docker-compose: the hackathon's approved stack excludes extra infrastructure and requires the app to run without Docker, so services run directly with Maven.

---

## 11. Git and GitHub

Initialise the repository and make the first commit (from the `gramcare` folder):

```bash
git init -b main
git add .
git commit -m "Initial GramCare implementation"
```

`.gitignore` already excludes `target/`, IDE files and local credential overrides. Before committing, check `git status` shows no `.env` or `application-local.properties`.

Push to GitHub (create an empty repository named `gramcare` on github.com first):

```bash
git remote add origin https://github.com/<your-username>/gramcare.git
git push -u origin main
```

Nothing has been pushed to GitHub yet.

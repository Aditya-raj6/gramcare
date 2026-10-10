# HealthHarbor: Architecture & Design

## 1. Goals and constraints
* **Domain:** rural primary healthcare and sanitation. Several teams (clinical, pharmacy, sanitation, water/PHED) own different data, but officers need one consolidated picture.
* **Mandated stack:** Java + Spring Boot, microservices, REST, a relational database, and a web UI.
* **Operational reality:** small district IT teams and unreliable connectivity. The system must degrade gracefully: if one service is down, the dashboard still works and only that service's section is missing.

## 2. Service decomposition

The services are split along **bounded contexts that match the teams who own the data**:

| Service | Port | Database | Owns | Talks to |
|---------|------|----------|------|----------|
| discovery-server | 8761 | – | Eureka registry | – |
| api-gateway | 8080 | – | Routing, JWT validation, role-based access, CORS | all (via `lb://`) |
| auth-service | 8081 | hh_auth | Users, roles, credentials, JWT issuance | – |
| patient-service | 8082 | hh_patient | Residents, medical records, visit history | – |
| healthcenter-service | 8083 | hh_healthcenter | Centres, facilities, workers, services, schedules | – |
| appointment-service | 8084 | hh_appointment | Appointments, consultations, treatment plans | patient, healthcenter |
| inventory-service | 8085 | hh_inventory | Catalogue, stock batches, ledger, replenishment | – |
| healthcamp-service | 8086 | hh_healthcamp | Camps, staff, beneficiaries, screenings, follow-ups | – |
| sanitation-service | 8087 | hh_sanitation | Facilities, inspections, maintenance, complaints | – |
| water-service | 8088 | hh_water | Water sources, quality tests, public-health issues, corrective actions | – |
| dashboard-service | 8089 | hh_dashboard | Field reports + routing; aggregation (no copy of others' data) | all |

**Database-per-service:** each service has its own PostgreSQL database and never reads another service's tables. A cross-service reference is stored as an ID plus a **display name copied at write time** (e.g. `Appointment.residentName`). Lists therefore render without fan-out calls, and a record stays readable if the other service is down.

## 3. Shared library (`hh-common`)
To keep the nine services consistent and small, `hh-common` provides:
* `BaseEntity`: identity primary key and created/updated timestamps.
* `CrudRepository<T>`, `CrudService<T>`, `CrudController<T>`: a generic REST surface (list with field filters, get, create, update with null-safe merge, `PATCH /{id}/status`, delete). Each service adds business rules only through hooks: `beforeSave`, `afterCreate`, `afterUpdate`, `validateTransition`, `onStatusChange`.
* `GlobalExceptionHandler` and `ApiError`: one error format everywhere (`404`, `400` with field errors, `409`, `503` for unavailable dependencies).
* `PendingAction` and `StatusUpdateRequest`: the cross-service contract for the consolidated officer view.

## 4. Key workflows

### 4.1 Field report routing (the illustrative example)
```
Health worker ──POST /api/field-reports──► dashboard-service
                                             │ save FieldReport + items (hh_dashboard)
                                             ├─ SANITATION_MAINTENANCE ─Feign─► sanitation  POST /maintenance  → MNT-…
                                             ├─ SANITATION_COMPLAINT   ─Feign─► sanitation  POST /complaints   → CMP-…
                                             ├─ MEDICINE_SHORTAGE      ─Feign─► inventory   POST /replenishments → REQ-…
                                             └─ WATER_ISSUE            ─Feign─► water       POST /issues       → PHI-…
```
* Each item records `targetService`, `targetId`, `targetReference` and `routingStatus` (ROUTED/FAILED with the downstream error message). Failed items can be retried.
* When reports are read, the dashboard syncs each open item's status from its owning service, so the reporter can see progress.

### 4.2 Consolidated pending actions
Every domain service exposes `GET …/pending`, which returns `PendingAction` items: open maintenance tasks, complaints, replenishment requests, public-health issues, corrective actions, camp follow-ups, today's and overdue appointments, and facility outages. `dashboard-service` calls all of them **in parallel** (each call has a timeout). It merges the results and sorts by priority, then due date. Each action carries its own `statusUrl` and `nextStatuses`, so the UI can move any item along its workflow without knowing which service owns it.

### 4.3 Domain automations
| Trigger | Automatic effect |
|---------|------------------|
| Stock issue / adjustment drops a batch to ≤ reorder level, out of stock, or expired | Replenishment request (`AUTO_LOW_STOCK`, HIGH or CRITICAL), unless one is already open |
| Replenishment → `FULFILLED` | Quantity added to the earliest-expiring valid batch (or a new batch) and a `RECEIPT` ledger entry |
| Consultation recorded | Appointment → `COMPLETED`; visit written to patient history (patient-service); follow-up appointment booked if a follow-up date is set |
| Screening ABNORMAL/CRITICAL + referred | Camp follow-up task (due in 2 days if critical, otherwise 7) |
| Inspection rated ≤ 2, DIRTY or UNUSABLE | Cleaning maintenance task (unless one is already active) |
| Maintenance task → `IN_PROGRESS` (structural work) / `COMPLETED` | Facility `UNDER_MAINTENANCE` / back to `OPERATIONAL`; cleaning resets cleanliness to CLEAN; linked complaints → `RESOLVED` |
| Water test evaluates UNSAFE (BIS 10500:2012) | Source → `CONTAMINATED`; public-health issue opened (CRITICAL if E. coli) |
| Corrective action → `COMPLETED` | Issue → `ACTION_TAKEN`; chlorination updates the source's last-chlorinated date |

Every status workflow is guarded by an explicit transition table (for example, an appointment can only be completed by recording a consultation, and a closed issue cannot be reopened).

## 5. Security
* **Authentication:** `auth-service` checks BCrypt-hashed passwords and issues an HS256 JWT (12 h) carrying `uid`, `role` and `name`.
* **Edge enforcement:** the gateway's `JwtAuthenticationFilter` rejects requests without a valid token (401). `AccessPolicy` limits write methods per module and role (403). The caller's identity is forwarded to downstream services as `X-User-Id`, `X-User-Name`, `X-User-FullName` and `X-User-Role`; services use it for audit fields such as "performed by".
* **Network:** in Docker only the gateway (and the UI) is published. Domain services are reachable only on the internal network.
* **Data minimisation:** only the last four digits of a resident's Aadhaar number are kept.
* The UI hides actions a role cannot perform, but the gateway remains the source of truth.

## 6. Data model (summary)
* **hh_patient:** `residents` (profile, medical summary, status), `medical_records` (typed entries), `visits` (vitals, diagnosis, optional `appointmentId`).
* **hh_healthcenter:** `health_centres`, `centre_facilities`, `health_workers`, `centre_services`, `operating_schedules` (centre hours or worker duty roster).
* **hh_appointment:** `appointments`, `consultations`, `consultation_treatments` (ordered element collection).
* **hh_inventory:** `medicines` (catalogue), `stock_items` (per centre & batch; stock status computed), `stock_transactions` (immutable ledger), `replenishment_requests`.
* **hh_healthcamp:** `health_camps`, `camp_staff`, `camp_beneficiaries`, `camp_screenings`, `camp_follow_ups`.
* **hh_sanitation:** `sanitation_facilities`, `sanitation_inspections`, `maintenance_tasks`, `sanitation_complaints`.
* **hh_water:** `water_sources`, `water_quality_tests`, `public_health_issues`, `corrective_actions`.
* **hh_dashboard:** `field_reports`, `field_report_items`.
* **hh_auth:** `app_users`.

The full DDL is in `database/schema/*.sql`. Human-readable references (`RES-00001`, `APT-00012`, `REQ-00003`, `MNT-00007`, `PHI-00002`, `FR-00001` …) are generated from the identity value.

## 7. Frontend
* React 18 + Vite + Ant Design 5 + Recharts. A single `axios` client attaches the JWT and redirects to login on a 401.
* A configuration-driven `CrudPage` component (columns, form fields, filters, status workflow, row actions, detail drawer, CSV export) drives most screens consistently. Purpose-built screens cover the dashboard, pending actions, field reports, consultations, stock movements and reports.
* Charts use a validated colour-blind-safe categorical palette and reserved status colours. Status is always shown as colour + icon + label.
* Responsive layout: the sidebar becomes a drawer on small screens.

## 8. Design decisions & trade-offs
| Decision | Why | Trade-off |
|----------|-----|-----------|
| Eureka + `lb://` routing | Services can scale or move without changing configuration | One more process to run |
| Synchronous Feign for routing and aggregation | Simple and transparent for a hackathon-scale deployment; failures are visible and retryable | A message broker (Kafka/RabbitMQ) would give asynchronous resilience at larger scale |
| Denormalised display names | Fast lists, works when the other service is down | A name changed at the source is not propagated retroactively |
| `ddl-auto=update` + seed data | Zero-step database setup for demos | Production should use versioned migrations (Flyway); the generated DDL in `database/schema` is a starting point |
| Generic CRUD layer | Nine services stay small and consistent | Complex queries still need custom repository methods (used for all stats) |

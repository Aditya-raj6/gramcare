# GramCare architecture

## Design goals

1. Real microservices, not a monolith with labels: six Spring Boot apps, six Maven builds, five databases, REST only.
2. Runs on one laptop: no Docker, service registry, gateway or broker required.
3. The core workflow — *report → record → route → assign → update → see on dashboard* — must be correct and honest under partial failure.

## Service boundaries and data ownership

| Service | Owns (tables) | References other services by id only |
|---|---|---|
| resident-service | residents, medical_records, health_centres, healthcare_workers, centre_services, operating_schedules | — |
| appointment-camp-service | appointments, consultations, treatments, health_camps, camp_staff, camp_beneficiaries, screening_records, follow_ups | resident_id, health_centre_id, worker_id |
| inventory-service | medicines, inventory_batches, stock_transactions, replenishment_requests | health_centre_id, action_item_id |
| sanitation-service | sanitation_facilities, cleanliness_inspections, sanitation_complaints, water_quality_observations, corrective_actions | action_item_id |
| action-service | action_items, action_history, department_assignments | source_service + source_reference_type + source_reference_id, health_centre_id |

Foreign keys exist only *within* a database. Cross-service references are plain ids.

## Unified action tracking

```
 Health worker           sanitation-service                   action-service
 ─────────────           ──────────────────                   ──────────────
 submit complaint ──▶ 1. save complaint (commit)
                      2. POST /api/actions ─────────────────▶ 3. look up (service,type,id)
                         {category: SANITATION,                  exists? → return existing
                          sourceService: sanitation-service,     else   → create, route to
                          sourceReferenceType:                            department by category,
                            SANITATION_COMPLAINT,                         default due date by
                          sourceReferenceId: 42}                          priority, history entry
                      4. store actionItemId on complaint ◀──── 201 {id: 17}
 sees complaint + link
```

Medicine shortages follow the same path from inventory-service (`REPLENISHMENT_REQUEST`), either when a user files a replenishment request or automatically when issuing stock drops a medicine below its minimum (only one open request per medicine). Out-of-range water observations do the same (`WATER_QUALITY_OBSERVATION`).

### Failure handling

* **Action-service down during step 2:** the complaint is still saved (it is the source of truth), but `actionItemId` stays null. The UI shows **Not linked** with a *Retry link* button — never a false "success".
* **Reconciliation:** a `@Scheduled` job in sanitation- and inventory-service retries all unlinked records every 60 s, so start-up order doesn't matter.
* **Idempotency:** a unique constraint on `(source_service, source_reference_type, source_reference_id)` plus a lookup-before-insert means retries (manual, scheduled, or a network retry after a lost response) return the same action item rather than duplicating it. Manual reports from the UI use a per-form token as their reference id, so a double-submit is also de-duplicated.
* **Dashboard aggregation:** action-service calls the other services for counts; each call is isolated, a failure contributes 0 and is reported in `serviceAvailability`, which the UI turns into a visible warning.

### Routing and lifecycle

* On creation the item is routed to a **department** using `department_assignments` (category → department, with a suggested assignee). Status stays `OPEN`.
* Assigning a **responsible person** moves `OPEN → ASSIGNED`.
* Status updates (`IN_PROGRESS`, `RESOLVED`, `CLOSED`, or reopening) each write an `action_history` row with previous/new status, who (the demo role) and notes. Resolved items stay in the register for history and reports.
* Due date defaults by priority: CRITICAL 1 day, HIGH 3, MEDIUM 7, LOW 14. Items past due and not resolved are flagged **overdue**.

## Inventory rules

* Stock lives in batches; total usable stock excludes expired batches.
* Issuing deducts from the earliest-expiring unexpired batch first, and is rejected with `400` if the request exceeds usable stock, so quantities never go negative (also enforced by a `CHECK (quantity >= 0)` in the reference DDL).
* Every stock-in and issue writes a `stock_transactions` row.

## Web application

* Spring MVC `@Controller`s render Thymeleaf templates; REST clients (`*Client` classes extending `AbstractApiClient`) call the services with timeouts.
* Backend JSON is consumed as maps (wrapped in `LenientMap` so a missing field renders blank instead of failing the template).
* Shared fragments: sidebar, top bar (page title + demo role), footer, service-error banner. Shared `styles.css` (navy/teal, CSS-only bar charts, responsive) and `app.js` (confirmations, required-field feedback, mobile sidebar).
* Backend failures on list pages show an inline banner with the rest of the page still usable; failures on form submission show an error page with the backend's own message (e.g. "Cannot issue 50 units… only 40 unexpired units are in stock").

## Testing approach

Service-layer tests run with `@SpringBootTest` on the `test` profile (in-memory H2 in PostgreSQL mode). Other services are deliberately *not* running during tests, which also exercises the degraded paths (complaint saved without link; dashboard figures reported unavailable).

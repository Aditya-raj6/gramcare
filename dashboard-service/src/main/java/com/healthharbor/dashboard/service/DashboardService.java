package com.healthharbor.dashboard.service;

import com.healthharbor.common.dto.PendingAction;
import com.healthharbor.dashboard.client.ServiceClients.*;
import com.healthharbor.dashboard.entity.FieldReport;
import com.healthharbor.dashboard.entity.FieldReportItem;
import com.healthharbor.dashboard.repository.FieldReportRepository;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Aggregates statistics and outstanding work from all microservices in parallel. A failing service
 * degrades only its own section of the dashboard.
 */
@Slf4j
@Service
public class DashboardService {

    private static final List<String> PRIORITY_ORDER = List.of("CRITICAL", "HIGH", "MEDIUM", "LOW");
    private static final long TIMEOUT_SECONDS = 6;

    private final PatientClient patients;
    private final HealthCentreClient centres;
    private final AppointmentClient appointments;
    private final InventoryClient inventory;
    private final HealthCampClient camps;
    private final SanitationClient sanitation;
    private final WaterClient water;
    private final FieldReportRepository fieldReports;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    public DashboardService(PatientClient patients, HealthCentreClient centres, AppointmentClient appointments,
                            InventoryClient inventory, HealthCampClient camps, SanitationClient sanitation,
                            WaterClient water, FieldReportRepository fieldReports) {
        this.patients = patients;
        this.centres = centres;
        this.appointments = appointments;
        this.inventory = inventory;
        this.camps = camps;
        this.sanitation = sanitation;
        this.water = water;
        this.fieldReports = fieldReports;
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    public Map<String, Object> summary() {
        Map<String, CompletableFuture<Object>> calls = new LinkedHashMap<>();
        calls.put("patients", async(patients::stats));
        calls.put("healthCentres", async(centres::stats));
        calls.put("appointments", async(appointments::stats));
        calls.put("inventory", async(inventory::stats));
        calls.put("healthCamps", async(camps::stats));
        calls.put("sanitation", async(sanitation::stats));
        calls.put("water", async(water::stats));
        CompletableFuture<Object> lowStock = async(inventory::attention);
        CompletableFuture<List<PendingAction>> pending = CompletableFuture.supplyAsync(this::pendingActions, executor);

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, String> health = new LinkedHashMap<>();
        calls.forEach((name, future) -> {
            Object value = await(future);
            health.put(name, value == null ? "DOWN" : "UP");
            result.put(name, value == null ? Map.of("available", false) : value);
        });
        Object attention = await(lowStock);
        result.put("lowStockItems", attention == null ? List.of() : ((List<?>) attention).stream().limit(10).toList());

        List<PendingAction> actions = Optional.ofNullable(this.<List<PendingAction>>awaitTyped(pending)).orElse(List.of());
        Map<String, Object> pendingSummary = new LinkedHashMap<>();
        pendingSummary.put("total", actions.size());
        pendingSummary.put("bySource", actions.stream().collect(Collectors.groupingBy(PendingAction::source, TreeMap::new, Collectors.counting())));
        pendingSummary.put("byPriority", actions.stream().collect(Collectors.groupingBy(PendingAction::priority, LinkedHashMap::new, Collectors.counting())));
        pendingSummary.put("byVillage", actions.stream().filter(a -> a.village() != null)
                .collect(Collectors.groupingBy(PendingAction::village, TreeMap::new, Collectors.counting())));
        pendingSummary.put("critical", actions.stream().filter(a -> "CRITICAL".equals(a.priority())).limit(8).toList());
        result.put("pending", pendingSummary);

        List<FieldReport> reports = fieldReports.findAll();
        result.put("fieldReports", Map.of(
                "total", reports.size(),
                "items", reports.stream().mapToLong(r -> r.getItems().size()).sum(),
                "failedRouting", reports.stream().flatMap(r -> r.getItems().stream())
                        .filter(i -> i.getRoutingStatus() == FieldReportItem.RoutingStatus.FAILED).count()));
        result.put("serviceHealth", health);
        result.put("generatedAt", LocalDateTime.now());
        return result;
    }

    /** Consolidated, priority-ordered list of outstanding work across every service. */
    public List<PendingAction> pendingActions() {
        List<CompletableFuture<Object>> calls = List.of(async(centres::pending), async(appointments::pending),
                async(inventory::pending), async(camps::pending), async(sanitation::pending), async(water::pending));
        List<PendingAction> all = new ArrayList<>();
        for (CompletableFuture<Object> call : calls) {
            Object value = await(call);
            if (value instanceof List<?> list) {
                list.forEach(o -> all.add((PendingAction) o));
            }
        }
        all.sort(Comparator.comparingInt((PendingAction a) -> rank(a.priority()))
                .thenComparing(PendingAction::dueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PendingAction::createdAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return all;
    }

    /** Village-level roll-up combining residents, complaints, unsafe water tests and pending actions. */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> villageReport() {
        Map<String, Object> patientStats = (Map<String, Object>) await(async(patients::stats));
        Map<String, Object> sanitationStats = (Map<String, Object>) await(async(sanitation::stats));
        Map<String, Object> waterStats = (Map<String, Object>) await(async(water::stats));
        Map<String, Long> pendingByVillage = pendingActions().stream().filter(a -> a.village() != null)
                .collect(Collectors.groupingBy(PendingAction::village, Collectors.counting()));
        Map<String, Object> residents = section(patientStats, "byVillage");
        Map<String, Object> complaints = section(sanitationStats, "complaintsByVillage");
        Map<String, Object> unsafe = section(waterStats, "unsafeTestsByVillage");

        Set<String> villages = new TreeSet<>();
        villages.addAll(residents.keySet());
        villages.addAll(complaints.keySet());
        villages.addAll(unsafe.keySet());
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String v : villages) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("village", v);
            row.put("residents", residents.getOrDefault(v, 0));
            row.put("sanitationComplaints", complaints.getOrDefault(v, 0));
            row.put("unsafeWaterTests", unsafe.getOrDefault(v, 0));
            row.put("pendingActions", pendingByVillage.getOrDefault(v, 0L));
            rows.add(row);
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(Map<String, Object> stats, String key) {
        return stats == null || !(stats.get(key) instanceof Map) ? Map.of() : (Map<String, Object>) stats.get(key);
    }

    private static int rank(String priority) {
        int i = PRIORITY_ORDER.indexOf(priority);
        return i < 0 ? PRIORITY_ORDER.size() : i;
    }

    private CompletableFuture<Object> async(Supplier<?> call) {
        return CompletableFuture.supplyAsync(call::get, executor);
    }

    private Object await(CompletableFuture<Object> future) {
        return awaitTyped(future);
    }

    private <T> T awaitTyped(CompletableFuture<?> future) {
        try {
            @SuppressWarnings("unchecked")
            T value = (T) future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return value;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException | TimeoutException ex) {
            log.warn("Dashboard aggregation call failed: {}", ex.getCause() == null ? ex.toString() : ex.getCause().getMessage());
            return null;
        }
    }
}

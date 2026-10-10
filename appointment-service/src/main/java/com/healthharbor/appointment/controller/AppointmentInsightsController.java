package com.healthharbor.appointment.controller;

import com.healthharbor.appointment.entity.Appointment;
import com.healthharbor.appointment.entity.Appointment.AppointmentStatus;
import com.healthharbor.appointment.repository.AppointmentRepository;
import com.healthharbor.appointment.repository.ConsultationRepository;
import com.healthharbor.common.dto.PendingAction;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment insights")
public class AppointmentInsightsController {

    private final AppointmentRepository appointments;
    private final ConsultationRepository consultations;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        LocalDate today = LocalDate.now();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalAppointments", appointments.count());
        stats.put("todayAppointments", appointments.countByAppointmentDate(today));
        stats.put("upcoming7Days", appointments.countByAppointmentDateBetweenAndStatus(today, today.plusDays(7), AppointmentStatus.SCHEDULED));
        stats.put("totalConsultations", consultations.count());
        stats.put("byStatus", toMap(appointments.countByStatus()));
        stats.put("byType", toMap(appointments.countByType()));
        stats.put("byCentre", toMap(appointments.countByCentre()));
        stats.put("dailyAppointments", toMap(appointments.dailyCounts(today.minusDays(14), today.plusDays(14))));
        stats.put("topDiagnoses", toMap(consultations.topDiagnoses(PageRequest.of(0, 6))));
        return stats;
    }

    /** Appointments due today or overdue that still need action. */
    @GetMapping("/pending")
    public List<PendingAction> pending() {
        LocalDate today = LocalDate.now();
        List<PendingAction> actions = new ArrayList<>();
        for (Appointment a : appointments.findByStatusInAndAppointmentDateLessThanEqualOrderByAppointmentDateAscAppointmentTimeAsc(
                EnumSet.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CHECKED_IN), today)) {
            boolean overdue = a.getAppointmentDate().isBefore(today);
            actions.add(new PendingAction("appointment", overdue ? "Overdue Appointment" : "Today's Appointment", a.getId(),
                    a.getAppointmentNumber(), a.getResidentName() + " - " + a.getType().name().replace('_', ' ').toLowerCase()
                    + " at " + a.getAppointmentTime(), a.getReason(), a.getVillage(),
                    a.getPriority() == Appointment.Priority.EMERGENCY ? "CRITICAL"
                            : a.getPriority() == Appointment.Priority.URGENT || overdue ? "HIGH" : "MEDIUM",
                    a.getStatus().name(), a.getAppointmentDate(), a.getCreatedAt(),
                    "/api/appointments/" + a.getId() + "/status",
                    a.getStatus() == AppointmentStatus.SCHEDULED ? List.of("CHECKED_IN", "CANCELLED", "NO_SHOW") : List.of("CANCELLED"),
                    "/appointments"));
        }
        return actions;
    }

    private static Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        rows.forEach(r -> map.put(String.valueOf(r[0]), ((Number) r[1]).longValue()));
        return map;
    }
}

package com.gramcare.appointment;

import com.gramcare.appointment.dto.AppointmentRequest;
import com.gramcare.appointment.entity.Appointment;
import com.gramcare.appointment.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AppointmentServiceTests {

    @Autowired
    private AppointmentService appointmentService;

    @Test
    void createsAppointmentForFutureDateTime() {
        AppointmentRequest req = new AppointmentRequest();
        req.setResidentId(1L);
        req.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        req.setReason("Routine checkup");
        Appointment saved = appointmentService.create(req);

        assertNotNull(saved.getId());
        assertEquals(Appointment.AppointmentStatus.SCHEDULED, saved.getStatus());
    }

    @Test
    void rejectsAppointmentInThePast() {
        AppointmentRequest req = new AppointmentRequest();
        req.setResidentId(1L);
        req.setAppointmentDateTime(LocalDateTime.now().minusDays(1));

        assertThrows(IllegalArgumentException.class, () -> appointmentService.create(req));
    }
}

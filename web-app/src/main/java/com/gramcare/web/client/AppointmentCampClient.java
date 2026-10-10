package com.gramcare.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class AppointmentCampClient extends AbstractApiClient {

    public AppointmentCampClient(RestTemplate restTemplate, @Value("${appointment-camp-service.base-url}") String baseUrl) {
        super(restTemplate, baseUrl, "Appointment & Health Camp Service");
    }

    public List<Map<String, Object>> listAppointments(Long residentId) {
        return getList("/api/appointments" + (residentId != null ? "?residentId=" + residentId : ""));
    }

    public List<Map<String, Object>> todaysAppointments() { return getList("/api/appointments/today"); }

    public List<Map<String, Object>> upcomingAppointments() { return getList("/api/appointments/upcoming"); }

    public Map<String, Object> getAppointment(Long id) { return getObject("/api/appointments/" + id); }

    public Map<String, Object> createAppointment(Map<String, Object> body) { return post("/api/appointments", body); }

    public Map<String, Object> updateAppointmentStatus(Long id, Map<String, Object> body) { return put("/api/appointments/" + id, body); }

    public Map<String, Object> addConsultation(Long appointmentId, Map<String, Object> body) { return post("/api/appointments/" + appointmentId + "/consultations", body); }

    public List<Map<String, Object>> getConsultations(Long appointmentId) { return getList("/api/appointments/" + appointmentId + "/consultations"); }

    public Map<String, Object> addTreatment(Long consultationId, Map<String, Object> body) { return post("/api/consultations/" + consultationId + "/treatments", body); }

    public List<Map<String, Object>> getTreatments(Long consultationId) { return getList("/api/consultations/" + consultationId + "/treatments"); }

    public List<Map<String, Object>> listHealthCamps() { return getList("/api/health-camps"); }

    public List<Map<String, Object>> activeHealthCamps() { return getList("/api/health-camps/active"); }

    public Map<String, Object> getHealthCamp(Long id) { return getObject("/api/health-camps/" + id); }

    public Map<String, Object> createHealthCamp(Map<String, Object> body) { return post("/api/health-camps", body); }

    public Map<String, Object> updateCampStatus(Long id, Map<String, Object> body) { return put("/api/health-camps/" + id, body); }

    public Map<String, Object> assignStaff(Long campId, Map<String, Object> body) { return post("/api/health-camps/" + campId + "/staff", body); }

    public List<Map<String, Object>> getStaff(Long campId) { return getList("/api/health-camps/" + campId + "/staff"); }

    public Map<String, Object> registerBeneficiary(Long campId, Map<String, Object> body) { return post("/api/health-camps/" + campId + "/beneficiaries", body); }

    public List<Map<String, Object>> getBeneficiaries(Long campId) { return getList("/api/health-camps/" + campId + "/beneficiaries"); }

    public Map<String, Object> addScreening(Long beneficiaryId, Map<String, Object> body) { return post("/api/beneficiaries/" + beneficiaryId + "/screenings", body); }

    public List<Map<String, Object>> getScreenings(Long beneficiaryId) { return getList("/api/beneficiaries/" + beneficiaryId + "/screenings"); }

    public Map<String, Object> addFollowUp(Long beneficiaryId, Map<String, Object> body) { return post("/api/beneficiaries/" + beneficiaryId + "/follow-ups", body); }

    public List<Map<String, Object>> getFollowUps(Long beneficiaryId) { return getList("/api/beneficiaries/" + beneficiaryId + "/follow-ups"); }

    public List<Map<String, Object>> pendingFollowUps() { return getList("/api/beneficiaries/follow-ups/pending"); }
}

package com.gramcare.action.dto;

import java.util.Map;

public class DashboardSummary {
    private long totalResidents;
    private long activeHealthCentres;
    private long todaysAppointments;
    private long activeHealthCamps;
    private long lowStockMedicines;
    private long pendingReplenishments;
    private long openSanitationComplaints;
    private long waterQualityIssues;
    private long totalPendingActions;
    private Map<String, Long> actionsByCategory;
    private Map<String, Long> actionsByStatus;
    private Map<String, Boolean> serviceAvailability;

    public long getTotalResidents() { return totalResidents; }
    public void setTotalResidents(long totalResidents) { this.totalResidents = totalResidents; }
    public long getActiveHealthCentres() { return activeHealthCentres; }
    public void setActiveHealthCentres(long activeHealthCentres) { this.activeHealthCentres = activeHealthCentres; }
    public long getTodaysAppointments() { return todaysAppointments; }
    public void setTodaysAppointments(long todaysAppointments) { this.todaysAppointments = todaysAppointments; }
    public long getActiveHealthCamps() { return activeHealthCamps; }
    public void setActiveHealthCamps(long activeHealthCamps) { this.activeHealthCamps = activeHealthCamps; }
    public long getLowStockMedicines() { return lowStockMedicines; }
    public void setLowStockMedicines(long lowStockMedicines) { this.lowStockMedicines = lowStockMedicines; }
    public long getPendingReplenishments() { return pendingReplenishments; }
    public void setPendingReplenishments(long pendingReplenishments) { this.pendingReplenishments = pendingReplenishments; }
    public long getOpenSanitationComplaints() { return openSanitationComplaints; }
    public void setOpenSanitationComplaints(long openSanitationComplaints) { this.openSanitationComplaints = openSanitationComplaints; }
    public long getWaterQualityIssues() { return waterQualityIssues; }
    public void setWaterQualityIssues(long waterQualityIssues) { this.waterQualityIssues = waterQualityIssues; }
    public long getTotalPendingActions() { return totalPendingActions; }
    public void setTotalPendingActions(long totalPendingActions) { this.totalPendingActions = totalPendingActions; }
    public Map<String, Long> getActionsByCategory() { return actionsByCategory; }
    public void setActionsByCategory(Map<String, Long> actionsByCategory) { this.actionsByCategory = actionsByCategory; }
    public Map<String, Long> getActionsByStatus() { return actionsByStatus; }
    public void setActionsByStatus(Map<String, Long> actionsByStatus) { this.actionsByStatus = actionsByStatus; }
    public Map<String, Boolean> getServiceAvailability() { return serviceAvailability; }
    public void setServiceAvailability(Map<String, Boolean> serviceAvailability) { this.serviceAvailability = serviceAvailability; }
}

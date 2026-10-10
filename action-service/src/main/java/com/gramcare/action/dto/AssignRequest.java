package com.gramcare.action.dto;

public class AssignRequest {
    private String assignedDepartment;
    private String assignedPerson;
    private String changedBy;

    public String getAssignedDepartment() { return assignedDepartment; }
    public void setAssignedDepartment(String assignedDepartment) { this.assignedDepartment = assignedDepartment; }
    public String getAssignedPerson() { return assignedPerson; }
    public void setAssignedPerson(String assignedPerson) { this.assignedPerson = assignedPerson; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
}

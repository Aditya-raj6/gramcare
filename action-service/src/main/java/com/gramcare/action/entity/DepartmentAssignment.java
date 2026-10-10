package com.gramcare.action.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "department_assignments")
public class DepartmentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private ActionItem.Category category;

    private String departmentName;
    private String defaultAssignee;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ActionItem.Category getCategory() { return category; }
    public void setCategory(ActionItem.Category category) { this.category = category; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getDefaultAssignee() { return defaultAssignee; }
    public void setDefaultAssignee(String defaultAssignee) { this.defaultAssignee = defaultAssignee; }
}

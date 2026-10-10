package com.gramcare.appointment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "camp_beneficiaries")
public class CampBeneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_camp_id", nullable = false)
    @JsonIgnore
    private HealthCamp healthCamp;

    /** References a Resident in resident-service, when the beneficiary is a registered resident. */
    private Long residentId;
    private String beneficiaryName;
    private Integer age;
    private String contactPhone;
    private boolean attended = true;

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ScreeningRecord> screenings = new ArrayList<>();

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FollowUp> followUps = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public HealthCamp getHealthCamp() { return healthCamp; }
    public void setHealthCamp(HealthCamp healthCamp) { this.healthCamp = healthCamp; }
    public Long getResidentId() { return residentId; }
    public void setResidentId(Long residentId) { this.residentId = residentId; }
    public String getBeneficiaryName() { return beneficiaryName; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }
    public List<ScreeningRecord> getScreenings() { return screenings; }
    public void setScreenings(List<ScreeningRecord> screenings) { this.screenings = screenings; }
    public List<FollowUp> getFollowUps() { return followUps; }
    public void setFollowUps(List<FollowUp> followUps) { this.followUps = followUps; }
}

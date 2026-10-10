package com.gramcare.appointment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "screening_records")
public class ScreeningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    @JsonIgnore
    private CampBeneficiary beneficiary;

    private String screeningType;
    private String result;
    @Column(length = 1000)
    private String remarks;
    private boolean referralNeeded = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CampBeneficiary getBeneficiary() { return beneficiary; }
    public void setBeneficiary(CampBeneficiary beneficiary) { this.beneficiary = beneficiary; }
    public String getScreeningType() { return screeningType; }
    public void setScreeningType(String screeningType) { this.screeningType = screeningType; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public boolean isReferralNeeded() { return referralNeeded; }
    public void setReferralNeeded(boolean referralNeeded) { this.referralNeeded = referralNeeded; }
}

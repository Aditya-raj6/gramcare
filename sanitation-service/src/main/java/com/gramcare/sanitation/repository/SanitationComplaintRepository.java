package com.gramcare.sanitation.repository;

import com.gramcare.sanitation.entity.SanitationComplaint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SanitationComplaintRepository extends JpaRepository<SanitationComplaint, Long> {
    List<SanitationComplaint> findByStatus(SanitationComplaint.ComplaintStatus status);
    List<SanitationComplaint> findByActionItemIdIsNull();
}

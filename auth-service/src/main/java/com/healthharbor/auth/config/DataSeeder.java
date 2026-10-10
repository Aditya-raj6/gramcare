package com.healthharbor.auth.config;

import com.healthharbor.auth.entity.AppUser;
import com.healthharbor.auth.entity.Role;
import com.healthharbor.auth.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "healthharbor.seed-data", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository repository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        user("admin", "admin123", "Dr. Meera Iyer", "dho@healthharbor.gov.in", Role.ADMIN, null, null);
        user("doctor", "doctor123", "Dr. Arjun Patil", "arjun.patil@healthharbor.gov.in", Role.DOCTOR, "Rampur", 1L);
        user("worker", "worker123", "Sunita Devi (ASHA)", "sunita.devi@healthharbor.gov.in", Role.HEALTH_WORKER, "Rampur", 1L);
        user("pharmacist", "pharma123", "Ravi Kumar", "ravi.kumar@healthharbor.gov.in", Role.PHARMACIST, "Rampur", 1L);
        user("sanitation", "sanit123", "Prakash Yadav", "prakash.yadav@healthharbor.gov.in", Role.SANITATION_OFFICER, "Sundarpur", null);
        log.info("Seeded default users (admin/admin123, doctor/doctor123, worker/worker123, pharmacist/pharma123, sanitation/sanit123)");
    }

    private void user(String username, String password, String name, String email, Role role, String village, Long centre) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setFullName(name);
        u.setEmail(email);
        u.setPhone("+91 98" + String.format("%08d", Math.abs(username.hashCode()) % 100000000));
        u.setRole(role);
        u.setAssignedVillage(village);
        u.setHealthCentreId(centre);
        repository.save(u);
    }
}

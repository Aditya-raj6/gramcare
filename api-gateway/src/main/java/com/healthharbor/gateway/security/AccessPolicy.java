package com.healthharbor.gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central role-based access policy enforced at the edge.
 * Every authenticated user may read; write operations are restricted per module.
 */
public final class AccessPolicy {

    public static final List<String> PUBLIC_PATHS = List.of("/api/auth/login", "/actuator/**");

    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final Set<String> CLINICAL = Set.of("ADMIN", "DOCTOR", "HEALTH_WORKER");
    private static final Set<String> SANITATION = Set.of("ADMIN", "SANITATION_OFFICER", "HEALTH_WORKER");
    private static final Set<String> INVENTORY = Set.of("ADMIN", "PHARMACIST", "DOCTOR", "HEALTH_WORKER");

    /** Ordered: the first matching pattern wins. */
    private static final Map<String, Set<String>> WRITE_RULES = new LinkedHashMap<>();

    static {
        WRITE_RULES.put("/api/auth/register", Set.of("ADMIN"));
        WRITE_RULES.put("/api/users/**", Set.of("ADMIN"));
        WRITE_RULES.put("/api/residents/**", CLINICAL);
        WRITE_RULES.put("/api/medical-records/**", CLINICAL);
        WRITE_RULES.put("/api/visits/**", CLINICAL);
        WRITE_RULES.put("/api/health-centres/**", Set.of("ADMIN"));
        WRITE_RULES.put("/api/facilities/**", Set.of("ADMIN"));
        WRITE_RULES.put("/api/health-workers/**", Set.of("ADMIN"));
        WRITE_RULES.put("/api/centre-services/**", Set.of("ADMIN"));
        WRITE_RULES.put("/api/schedules/**", Set.of("ADMIN"));
        WRITE_RULES.put("/api/appointments/**", CLINICAL);
        WRITE_RULES.put("/api/consultations/**", Set.of("ADMIN", "DOCTOR"));
        WRITE_RULES.put("/api/medicines/**", Set.of("ADMIN", "PHARMACIST"));
        WRITE_RULES.put("/api/stock/**", INVENTORY);
        WRITE_RULES.put("/api/replenishments/**", INVENTORY);
        WRITE_RULES.put("/api/health-camps/**", CLINICAL);
        WRITE_RULES.put("/api/camp-staff/**", CLINICAL);
        WRITE_RULES.put("/api/beneficiaries/**", CLINICAL);
        WRITE_RULES.put("/api/screenings/**", CLINICAL);
        WRITE_RULES.put("/api/follow-ups/**", CLINICAL);
        WRITE_RULES.put("/api/sanitation/**", SANITATION);
        WRITE_RULES.put("/api/water/**", SANITATION);
    }

    private AccessPolicy() {
    }

    public static boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(p -> MATCHER.match(p, path));
    }

    public static boolean isAllowed(HttpMethod method, String path, String role) {
        if ("ADMIN".equals(role) || HttpMethod.GET.equals(method) || HttpMethod.OPTIONS.equals(method)) {
            return true;
        }
        return WRITE_RULES.entrySet().stream()
                .filter(e -> MATCHER.match(e.getKey(), path) || MATCHER.match(e.getKey(), path + "/"))
                .findFirst()
                .map(e -> e.getValue().contains(role))
                .orElse(true);
    }
}

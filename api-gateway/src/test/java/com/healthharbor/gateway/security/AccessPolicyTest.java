package com.healthharbor.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

class AccessPolicyTest {

    @Test
    void everyAuthenticatedRoleCanRead() {
        assertThat(AccessPolicy.isAllowed(HttpMethod.GET, "/api/residents", "SANITATION_OFFICER")).isTrue();
        assertThat(AccessPolicy.isAllowed(HttpMethod.GET, "/api/stock", "HEALTH_WORKER")).isTrue();
    }

    @Test
    void writesAreRestrictedPerModule() {
        assertThat(AccessPolicy.isAllowed(HttpMethod.POST, "/api/residents", "HEALTH_WORKER")).isTrue();
        assertThat(AccessPolicy.isAllowed(HttpMethod.POST, "/api/residents", "SANITATION_OFFICER")).isFalse();
        assertThat(AccessPolicy.isAllowed(HttpMethod.PATCH, "/api/sanitation/maintenance/4/status", "SANITATION_OFFICER")).isTrue();
        assertThat(AccessPolicy.isAllowed(HttpMethod.PATCH, "/api/sanitation/maintenance/4/status", "PHARMACIST")).isFalse();
        assertThat(AccessPolicy.isAllowed(HttpMethod.POST, "/api/consultations", "HEALTH_WORKER")).isFalse();
        assertThat(AccessPolicy.isAllowed(HttpMethod.DELETE, "/api/users/3", "DOCTOR")).isFalse();
    }

    @Test
    void adminCanDoEverythingAndFieldReportsAreOpenToAllRoles() {
        assertThat(AccessPolicy.isAllowed(HttpMethod.DELETE, "/api/health-centres/1", "ADMIN")).isTrue();
        assertThat(AccessPolicy.isAllowed(HttpMethod.POST, "/api/field-reports", "PHARMACIST")).isTrue();
    }

    @Test
    void loginIsPublic() {
        assertThat(AccessPolicy.isPublic("/api/auth/login")).isTrue();
        assertThat(AccessPolicy.isPublic("/api/residents")).isFalse();
    }
}

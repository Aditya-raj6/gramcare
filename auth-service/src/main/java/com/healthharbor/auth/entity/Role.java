package com.healthharbor.auth.entity;

public enum Role {
    /** District / block health officer with full access and approval rights. */
    ADMIN,
    DOCTOR,
    /** ASHA / ANM / field health worker. */
    HEALTH_WORKER,
    PHARMACIST,
    SANITATION_OFFICER
}

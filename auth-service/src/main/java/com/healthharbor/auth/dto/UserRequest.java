package com.healthharbor.auth.dto;

import com.healthharbor.auth.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(@NotBlank @Size(min = 3, max = 60) String username,
                          @Size(min = 6, max = 100) String password,
                          @NotBlank String fullName,
                          @Email String email,
                          String phone,
                          @NotNull Role role,
                          String assignedVillage,
                          Long healthCentreId,
                          Boolean active) {
}

package com.healthharbor.auth.dto;

import com.healthharbor.auth.entity.AppUser;

public record LoginResponse(String token, long expiresInSeconds, AppUser user) {
}

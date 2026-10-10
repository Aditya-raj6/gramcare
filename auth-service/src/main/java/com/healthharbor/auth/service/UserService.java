package com.healthharbor.auth.service;

import com.healthharbor.auth.dto.*;
import com.healthharbor.auth.entity.AppUser;
import com.healthharbor.auth.repository.AppUserRepository;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        AppUser user = repository.findByUsernameIgnoreCase(request.username().trim())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BusinessException("Invalid username or password", HttpStatus.UNAUTHORIZED));
        if (!user.isActive()) {
            throw new BusinessException("Account is disabled. Contact the administrator.", HttpStatus.FORBIDDEN);
        }
        return new LoginResponse(jwtService.issue(user), jwtService.validitySeconds(), user);
    }

    public List<AppUser> list() {
        return repository.findAll(Sort.by("id"));
    }

    public AppUser get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public AppUser getByUsername(String username) {
        return repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + " not found"));
    }

    @Transactional
    public AppUser create(UserRequest request) {
        if (repository.existsByUsernameIgnoreCase(request.username())) {
            throw new BusinessException("Username already exists", HttpStatus.CONFLICT);
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessException("Password is required for new users");
        }
        AppUser user = new AppUser();
        user.setUsername(request.username().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        apply(user, request);
        return repository.save(user);
    }

    @Transactional
    public AppUser update(Long id, UserRequest request) {
        AppUser user = get(id);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        apply(user, request);
        return repository.save(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        AppUser user = getByUsername(username);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        repository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(get(id));
    }

    private void apply(AppUser user, UserRequest request) {
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setAssignedVillage(request.assignedVillage());
        user.setHealthCentreId(request.healthCentreId());
        if (request.active() != null) {
            user.setActive(request.active());
        }
    }
}

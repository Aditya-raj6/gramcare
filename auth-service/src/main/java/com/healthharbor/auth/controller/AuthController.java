package com.healthharbor.auth.controller;

import com.healthharbor.auth.dto.*;
import com.healthharbor.auth.entity.AppUser;
import com.healthharbor.auth.entity.Role;
import com.healthharbor.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    /** Identity is resolved by the API gateway from the JWT and forwarded as X-User-Name. */
    @GetMapping("/me")
    public AppUser me(@RequestHeader("X-User-Name") String username) {
        return userService.getByUsername(username);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AppUser register(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestHeader("X-User-Name") String username,
                               @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(username, request);
    }

    @GetMapping("/roles")
    public List<Role> roles() {
        return List.of(Role.values());
    }
}

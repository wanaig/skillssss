package com.harness.platform.controller;

import com.harness.platform.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");
            String name = body.getOrDefault("name", email.split("@")[0]);
            if (email == null || password == null || password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "邮箱和密码为必填项，密码至少6位"));
            }
            return ResponseEntity.ok(authService.register(email, password, name));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");
            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "邮箱和密码为必填项"));
            }
            return ResponseEntity.ok(authService.login(email, password));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}

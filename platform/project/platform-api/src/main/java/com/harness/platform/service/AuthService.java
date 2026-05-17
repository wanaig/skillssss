package com.harness.platform.service;

import com.harness.platform.model.User;
import com.harness.platform.repository.UserRepository;
import com.harness.platform.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> register(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }
        User user = new User(email, passwordEncoder.encode(password), name);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return Map.of(
            "token", token,
            "userId", user.getId(),
            "email", user.getEmail(),
            "name", user.getName()
        );
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("邮箱或密码错误"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("邮箱或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return Map.of(
            "token", token,
            "userId", user.getId(),
            "email", user.getEmail(),
            "name", user.getName()
        );
    }
}

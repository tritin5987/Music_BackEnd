package org.example.bts_backend.Controller;


import org.example.bts_backend.Models.User;
import org.example.bts_backend.Repository.UserRepository;
import org.example.bts_backend.Services.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    public AuthController(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        return ResponseEntity.ok("Đăng ký thành công");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> loginRequest) {
        User user = userRepository.findByUsername(loginRequest.get("username"))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        if (passwordEncoder.matches(loginRequest.get("password"), user.getPassword())) {
            // Trích xuất quyền từ trường role
            List<String> roles = List.of(user.getRole());  // Lấy trực tiếp từ role (String)

            // Tạo token kèm quyền
            String token = jwtService.generateToken(user.getUsername(), roles);
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(401).body("Sai thông tin đăng nhập");
    }


}

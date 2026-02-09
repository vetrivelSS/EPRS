package com.logincontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    // 1. REGISTER API
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    // 2. LOGIN API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        
        // 1. Find user (Email or Phone)
        User user = userRepository.findByEmailOrPhone(loginRequest.getIdentifier(), loginRequest.getIdentifier())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        // 2. Check Password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new BaseResponse(401, "Invalid credentials", null));
        }

        // 3. Generate JWT & Cookie
        String token = jwtUtils.generateToken(user.getEmail());
        ResponseCookie cookie = ResponseCookie.from("jwtToken", token)
                .httpOnly(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();

        // 4. Data Object (Postman image-la irukura mathiri)
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("phone", user.getPhone());
        // Note: Security kaaga password-ah response-la anupama irukarthu nallathu

        // 5. Final Response structure
        BaseResponse response = new BaseResponse(200, "Login successful", userData);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }
}

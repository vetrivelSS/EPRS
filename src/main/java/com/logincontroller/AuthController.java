package com.logincontroller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	private EmailService emailService;

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
    

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // REAL EMAIL SENDING
        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new BaseResponse(500, "Error sending email", null));
        }

        Map<String, Object> details = new HashMap<>();
        details.put("email", user.getEmail());
        List<Map<String, Object>> dataList = List.of(details);

        return ResponseEntity.ok(new BaseResponse(200, "OTP sent to your email", dataList));
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Check OTP and Expiry
        if (user.getOtp().equals(request.getOtp()) && user.getOtpExpiry().isAfter(LocalDateTime.now())) {
            
            Map<String, Object> details = new HashMap<>();
            details.put("email", user.getEmail());
            details.put("isVerified", true);

            List<Map<String, Object>> dataList = List.of(details);
            return ResponseEntity.ok(new BaseResponse(200, "OTP Verified", dataList));
        }

        return ResponseEntity.status(400).body(new BaseResponse(400, "Invalid or Expired OTP", new ArrayList<>()));
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        
        // 1. New and Confirm Password match check
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(400).body(new BaseResponse(400, "Passwords do not match", new ArrayList<>()));
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Update Password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtp(null); // Safety kaaga OTP-ah clear pannuvom
        userRepository.save(user);

        Map<String, Object> details = new HashMap<>();
        details.put("email", user.getEmail());
        details.put("updateStatus", "Success");

        List<Map<String, Object>> dataList = List.of(details);

        return ResponseEntity.ok(new BaseResponse(200, "Password reset successfully", dataList));
    }
   
}

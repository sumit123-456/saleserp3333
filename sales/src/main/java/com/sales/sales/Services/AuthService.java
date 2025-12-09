package com.sales.sales.Services;

import com.sales.sales.Entity.Role;
import com.sales.sales.Entity.User;
import com.sales.sales.Repositories.RoleRepository;
import com.sales.sales.Repositories.UserRepository;
import com.sales.sales.dto.LoginRequest;
import com.sales.sales.dto.LoginResponse;
import com.sales.sales.dto.UserRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    // Admin credentials
    private final String ADMIN_EMAIL = "admin@saleserp.com";
    private final String ADMIN_PASSWORD = "admin123";
    private final String ADMIN_NAME = "System Administrator";
    
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    @PostConstruct
    public void initAdminUser() {
        try {
            // Create ADMIN role if not exists
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setRoleName("ADMIN");
                        return roleRepository.save(newRole);
                    });
            
            // Create EMPLOYEE role if not exists
            Role employeeRole = roleRepository.findByRoleName("EMPLOYEE")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setRoleName("EMPLOYEE");
                        return roleRepository.save(newRole);
                    });
            
            // Check if admin user already exists
            if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
                String encodedPassword = passwordEncoder.encode(ADMIN_PASSWORD);
                
                User admin = User.builder()
                        .fullName(ADMIN_NAME)
                        .email(ADMIN_EMAIL)
                        .password(encodedPassword)
                        .callTarget(0)
                        .monthlyTarget(0)
                        .teamAllocation("Administration")
                        .role(adminRole)
                        .build();
                
                userRepository.save(admin);
                log.info("✅ Admin user created successfully: {}", ADMIN_EMAIL);
                
                // Verify password can be matched
                boolean passwordMatches = passwordEncoder.matches(ADMIN_PASSWORD, encodedPassword);
                log.info("🔐 Password verification: {} = {}", ADMIN_PASSWORD, passwordMatches);
            } else {
                log.info("✅ Admin user already exists: {}", ADMIN_EMAIL);
            }
            
        } catch (Exception e) {
            log.error("❌ Error initializing admin user: {}", e.getMessage(), e);
        }
    }
    
    // ✅ NEW: Get user email from token
    public String getUserEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error parsing token: {}", e.getMessage());
            return null;
        }
    }
    
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("AuthService : login() : Attempting login for email: {}", loginRequest.getEmail());
        
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        
        if (userOptional.isEmpty()) {
            log.error("Login failed: User not found with email: {}", loginRequest.getEmail());
            return LoginResponse.builder()
                    .success(false)
                    .message("Invalid credentials")
                    .build();
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.error("Login failed: Invalid password for user: {}", loginRequest.getEmail());
            return LoginResponse.builder()
                    .success(false)
                    .message("Invalid credentials")
                    .build();
        }
        
        // Generate token
        String token = generateToken(user.getEmail(), user.getRole().getRoleName());
        
        return LoginResponse.builder()
                .success(true)
                .message("Login successful")
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getRoleName())
                .userId(user.getUserId())
                .build();
    }
    
    private String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public Boolean register(UserRequest userRequest) {
        log.info("AuthService : register() : Registering new user: {}", userRequest.getEmail());
        
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(userRequest.getEmail())) {
                throw new RuntimeException("Email already exists: " + userRequest.getEmail());
            }
            
            // Find role by ID
            Role role = roleRepository.findById(userRequest.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with ID: " + userRequest.getRoleId()));
            
            // Create new user
            User user = User.builder()
                    .fullName(userRequest.getFullName())
                    .email(userRequest.getEmail())
                    .password(passwordEncoder.encode(userRequest.getPassword()))
                    .phoneNumber(userRequest.getPhoneNumber())
                    .callTarget(userRequest.getCallTarget() != null ? userRequest.getCallTarget() : 0)
                    .monthlyTarget(userRequest.getMonthlyTarget() != null ? userRequest.getMonthlyTarget() : 0)
                    .teamAllocation(userRequest.getTeamAllocation())
                    .role(role)
                    .build();
            
            userRepository.save(user);
            log.info("User registered successfully: {}", user.getEmail());
            return true;
            
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
    
    public String getUserRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.error("Error parsing token: {}", e.getMessage());
            return null;
        }
    }
    
    public long getTotalUsers() {
        return userRepository.count();
    }
}
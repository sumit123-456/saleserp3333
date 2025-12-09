package com.sales.sales.Controller;

import com.sales.sales.Services.AuthService;
import com.sales.sales.Services.UserService;
import com.sales.sales.dto.LoginRequest;
import com.sales.sales.dto.LoginResponse;
import com.sales.sales.dto.UserRequest;
import com.sales.sales.validation.CommonUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        log.info("AuthController : loginUser() : Execution Start");
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return CommonUtil.createErrorResponse("Validation errors: " + errors, HttpStatus.BAD_REQUEST);
        }
        
        LoginResponse loginResponse = authService.login(loginRequest);

        if (ObjectUtils.isEmpty(loginResponse) || !loginResponse.getSuccess()) {
            log.info("Error : {}", "Login Unsuccessful - Invalid credentials");
            return CommonUtil.createErrorResponseMessage("Login Failed: Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        log.info("Login successful for user: {}", loginResponse.getEmail());
        return CommonUtil.createBuildResponse(loginResponse, HttpStatus.OK);
    }

    // Admin-only registration endpoint
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest userRequest, 
                                          BindingResult bindingResult) {
        log.info("AuthController : registerUser() : Admin registering new user");
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return CommonUtil.createErrorResponse(errors.toString(), HttpStatus.BAD_REQUEST);
        }
        
        try {
            Boolean register = authService.register(userRequest);
            if (register) {
                log.info("Success : {}", "User registered successfully");
                return CommonUtil.createBuildResponse("User registered successfully", HttpStatus.CREATED);
            }
            return CommonUtil.createErrorResponse("Registration Failed", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            return CommonUtil.createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user-role")
    public ResponseEntity<?> getUserRole(@RequestHeader("Authorization") String token) {
        log.info("AuthController : getUserRole() : Execution Start");
        try {
            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String userRole = authService.getUserRoleFromToken(token);
            return CommonUtil.createBuildResponse(Map.of("role", userRole), HttpStatus.OK);
        } catch (Exception e) {
            return CommonUtil.createErrorResponse("Invalid token", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/count")
    public Map<String, Object> getUserCount() {
        long count = authService.getTotalUsers();
        return Map.of("totalUsers", count);
    }
    
    // ✅ NEW: Get current user endpoint
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("AuthController : getCurrentUser() : Execution Start");
        
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return CommonUtil.createErrorResponse("Authorization token required", HttpStatus.UNAUTHORIZED);
            }
            
            token = token.substring(7);
            String email = authService.getUserEmailFromToken(token);
            
            if (email == null) {
                return CommonUtil.createErrorResponse("Invalid token", HttpStatus.UNAUTHORIZED);
            }
            
            var user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> userData = Map.of(
                "userId", user.getUserId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber(),
                "teamAllocation", user.getTeamAllocation() != null ? user.getTeamAllocation() : "N/A",
                "callTarget", user.getCallTarget() != null ? user.getCallTarget() : 0,
                "monthlyTarget", user.getMonthlyTarget() != null ? user.getMonthlyTarget() : 0,
                "role", user.getRole() != null ? user.getRole().getRoleName() : "No Role",
                "profileInitial", user.getFullName().substring(0, 1).toUpperCase()
            );
            
            return CommonUtil.createBuildResponse(userData, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return CommonUtil.createErrorResponse("Error fetching user data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ✅ NEW: Get all users (Admin only)
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        log.info("AuthController : getAllUsers() : Fetching all users");
        try {
            var users = userService.getAllUsersWithRole();
            return CommonUtil.createBuildResponse(users, HttpStatus.OK);
        } catch (Exception e) {
            return CommonUtil.createErrorResponse("Error fetching users: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ✅ NEW: Get user by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        log.info("AuthController : getUserById() : Fetching user with ID: {}", id);
        try {
            var user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
            var userResponse = userService.convertToUserResponse(user);
            return CommonUtil.createBuildResponse(userResponse, HttpStatus.OK);
        } catch (Exception e) {
            return CommonUtil.createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    // ✅ NEW: Update user profile
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> updates) {
        log.info("AuthController : updateUser() : Updating user with ID: {}", id);
        try {
            var user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Update allowed fields
            if (updates.containsKey("fullName")) {
                user.setFullName((String) updates.get("fullName"));
            }
            if (updates.containsKey("phoneNumber")) {
                user.setPhoneNumber((String) updates.get("phoneNumber"));
            }
            if (updates.containsKey("teamAllocation")) {
                user.setTeamAllocation((String) updates.get("teamAllocation"));
            }
            if (updates.containsKey("callTarget")) {
                user.setCallTarget((Integer) updates.get("callTarget"));
            }
            if (updates.containsKey("monthlyTarget")) {
                user.setMonthlyTarget((Integer) updates.get("monthlyTarget"));
            }
            
            var updatedUser = userService.updateUser(user);
            return CommonUtil.createBuildResponse(userService.convertToUserResponse(updatedUser), HttpStatus.OK);
            
        } catch (Exception e) {
            return CommonUtil.createErrorResponse("Error updating user: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    // ✅ NEW: Search users by name
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String name) {
        log.info("AuthController : searchUsers() : Searching users by name: {}", name);
        try {
            var users = userService.searchUsersByName(name);
            var userResponses = users.stream()
                    .map(userService::convertToUserResponse)
                    .collect(java.util.stream.Collectors.toList());
            return CommonUtil.createBuildResponse(userResponses, HttpStatus.OK);
        } catch (Exception e) {
            return CommonUtil.createErrorResponse("Error searching users: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
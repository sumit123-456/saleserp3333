package com.sales.sales.Services;

import com.sales.sales.Entity.User;
import com.sales.sales.Repositories.UserRepository;
import com.sales.sales.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public Optional<User> getUserById(Integer id) {
        log.info("UserService : getUserById() : Fetching user by ID: {}", id);
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByEmail(String email) {
        log.info("UserService : getUserByEmail() : Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    public User updateUser(User user) {
        log.info("UserService : updateUser() : Updating user with ID: {}", user.getUserId());
        return userRepository.save(user);
    }
    
    public User getCurrentUser(String email) {
        log.info("UserService : getCurrentUser() : Getting current user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    public long getTotalUserCount() {
        log.info("UserService : getTotalUserCount() : Getting total user count");
        return userRepository.count();
    }
    
    public List<User> getAllUsers() {
        log.info("UserService : getAllUsers() : Fetching all users");
        return userRepository.findAll();
    }
    
    public List<UserResponse> getAllUsersWithRole() {
        log.info("UserService : getAllUsersWithRole() : Fetching all users with role info");
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
    
    public List<User> getUsersByTeam(String teamAllocation) {
        log.info("UserService : getUsersByTeam() : Fetching users by team: {}", teamAllocation);
        return userRepository.findByTeamAllocation(teamAllocation);
    }
    
    public List<User> getUsersByRole(String roleName) {
        log.info("UserService : getUsersByRole() : Fetching users by role: {}", roleName);
        return userRepository.findByRole_RoleName(roleName);
    }
    
    public List<String> getAllDistinctTeams() {
        log.info("UserService : getAllDistinctTeams() : Fetching all distinct teams");
        return userRepository.findDistinctTeamAllocations();
    }
    
    public long countUsersByTeam(String teamAllocation) {
        log.info("UserService : countUsersByTeam() : Counting users in team: {}", teamAllocation);
        return userRepository.countByTeamAllocation(teamAllocation);
    }
    
    public User createUser(User user) {
        log.info("UserService : createUser() : Creating new user with email: {}", user.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with email " + user.getEmail() + " already exists");
        }
        
        return userRepository.save(user);
    }
    
    public void deleteUser(Integer userId) {
        log.info("UserService : deleteUser() : Deleting user with ID: {}", userId);
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        } else {
            throw new RuntimeException("User with ID " + userId + " not found");
        }
    }
    
    public List<User> searchUsersByName(String name) {
        log.info("UserService : searchUsersByName() : Searching users by name: {}", name);
        return userRepository.findByFullNameContainingIgnoreCase(name);
    }
    
    public UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole().getRoleName() : "No Role")
                .callTarget(user.getCallTarget())
                .monthlyTarget(user.getMonthlyTarget())
                .teamAllocation(user.getTeamAllocation())
                .build();
    }
    
    public List<User> getTopPerformers() {
        log.info("UserService : getTopPerformers() : Fetching top performers");
        return userRepository.findTopPerformersByMonthlyTarget();
    }
    
    public Double getAverageCallTarget() {
        log.info("UserService : getAverageCallTarget() : Calculating average call target");
        return userRepository.findAverageCallTarget();
    }
    
    public Double getAverageMonthlyTarget() {
        log.info("UserService : getAverageMonthlyTarget() : Calculating average monthly target");
        return userRepository.findAverageMonthlyTarget();
    }
    
    public List<User> getUsersWithoutTeam() {
        log.info("UserService : getUsersWithoutTeam() : Fetching users without team allocation");
        return userRepository.findByTeamAllocationIsNull();
    }
    
    public List<User> getUsersWithTeam() {
        log.info("UserService : getUsersWithTeam() : Fetching users with team allocation");
        return userRepository.findByTeamAllocationIsNotNull();
    }
}
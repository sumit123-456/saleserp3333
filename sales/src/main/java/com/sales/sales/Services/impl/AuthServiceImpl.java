package com.sales.sales.Services.impl;

import com.sales.sales.dto.LoginRequest;
import com.sales.sales.dto.LoginResponse;
import com.sales.sales.dto.UserRequest;
import com.sales.sales.dto.UserResponse;
import java.util.List;

public interface AuthServiceImpl {
    Boolean register(UserRequest userRequest, String url) throws Exception;
    LoginResponse login(LoginRequest loginRequest) throws Exception;
    long getTotalUsers();
    String getUserRoleFromToken(String token);
    String getUserEmailFromToken(String token);
    boolean isAdmin(String token);
    Boolean register(UserRequest userRequest);
    List<UserResponse> getAllEmployees();
    List<UserResponse> getAllUsers();
    UserResponse updateEmployee(Integer userId, UserRequest userRequest);
    void deleteEmployee(Integer userId);
    boolean validateToken(String token);
    String refreshToken(String oldToken);
}
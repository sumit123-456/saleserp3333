package com.sales.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Integer userId;
    private String fullName;
    private String email;
    private String role;
    private Integer callTarget;
    private Integer monthlyTarget;
    private String teamAllocation;
    private String token; // Add this field for JWT token

    // You might want to add these fields for better frontend handling
    private String message;
    private Boolean success;
}
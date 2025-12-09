package com.sales.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private Integer callTarget;
    private Integer monthlyTarget;
    private String teamAllocation;

    // You might want to add these for additional info
    private String createdAt; // If you have timestamp in entity
    private Boolean active;   // If you have status field
}
package com.sales.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Please provide a valid 10-digit phone number")
    private String phoneNumber;
    
    private Integer roleId;
    
    @Min(value = 0, message = "Call target must be positive or zero")
    private Integer callTarget;
    
    @Min(value = 0, message = "Monthly target must be positive or zero")
    private Integer monthlyTarget;
    
    private String teamAllocation;
}
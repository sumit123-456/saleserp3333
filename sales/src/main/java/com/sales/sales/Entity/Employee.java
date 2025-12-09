package com.sales.sales.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @Column(name = "emp_id")
    private String empId;

    private String empName;
    private String empCode;
    private String role;
    private String department;
    private Integer monthlyCallTarget;
    private Integer monthlyTarget;
    private LocalDate joinDate;
    private Integer achieved;
    private Integer callsMade;
    private Integer meetTarget;
    private String team;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}


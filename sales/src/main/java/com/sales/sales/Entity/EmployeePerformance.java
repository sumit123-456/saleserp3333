package com.sales.sales.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_performance")
@Data
public class EmployeePerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "performance_score")
    private Double performanceScore;

    @Column(name = "rating")
    private String rating;

    @Column(name = "assessment_date")
    private LocalDateTime assessmentDate;
}
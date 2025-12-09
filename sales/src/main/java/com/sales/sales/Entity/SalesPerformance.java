package com.sales.sales.Entity;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_performance")
@Data
public class SalesPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "month_year")
    private String monthYear;

    @Column(name = "sales_target")
    private Double salesTarget;

    @Column(name = "sales_achieved")
    private Double salesAchieved;

    @Column(name = "target_achievement")
    private Double targetAchievement;

    @Column(name = "region")
    private String region;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}


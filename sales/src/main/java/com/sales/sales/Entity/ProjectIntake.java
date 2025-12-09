package com.sales.sales.Entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_intake")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectIntake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String employeeName;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "emp_id", referencedColumnName = "emp_id")
    private Employee employee;
    @Transient // Not persisted in DB, just for input
    private String empId;
    private String projectName;
    private Long projectId;
    private String projectType;
    private String projectStatus;
    private String companyName;
    private Long leadId;
    private String intakeDate;
}

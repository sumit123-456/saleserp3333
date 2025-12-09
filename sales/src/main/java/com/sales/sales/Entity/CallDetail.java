package com.sales.sales.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "call_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long callId;
    @Column(name = "emp_id")
    private String empId;
    private LocalDate callDate;
    private String callType;
    private String disposition;
    private int duration;
    private String team;
}

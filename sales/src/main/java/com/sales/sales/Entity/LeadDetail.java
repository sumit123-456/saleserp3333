package com.sales.sales.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "lead_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leadId;

    private String empId;
    private String phone;
    private String source;
    private String convertedToDeal;
    private Float dealValue;
    private LocalDate createdDate = LocalDate.now();
}


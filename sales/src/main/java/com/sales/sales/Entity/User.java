package com.sales.sales.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    private String fullName;
    private String email;
    private String password;
    private String phoneNumber;
    private Integer callTarget;
    private Integer monthlyTarget;
    private String teamAllocation;
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}

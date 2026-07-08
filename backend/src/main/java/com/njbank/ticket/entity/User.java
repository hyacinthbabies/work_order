package com.njbank.ticket.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    private String role;
    
    private String department;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "skill_tags", columnDefinition = "TEXT")
    private String skillTags;
    
    @Column(name = "engineer_level")
    private String engineerLevel;
    
    @Column(name = "is_on_duty")
    private Boolean isOnDuty = false;
}
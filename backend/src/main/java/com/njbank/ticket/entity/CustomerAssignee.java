package com.njbank.ticket.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_assignee")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAssignee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_no", nullable = false)
    private String customerNo;
    
    @Column(name = "assignee_username", nullable = false)
    private String assigneeUsername;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
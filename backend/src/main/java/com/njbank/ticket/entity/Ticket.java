package com.njbank.ticket.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ticket_no", unique = true, nullable = false)
    private String ticketNo;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String type;
    
    private String urgency;
    
    private String channel;
    
    private String department;
    
    private String status;
    
    @Column(name = "customer_no")
    private String customerNo;
    
    @Column(name = "customer_name")
    private String customerName;
    
    private String phone;
    
    @Column(name = "account_no")
    private String accountNo;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "assigned_to")
    private String assignedTo;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @Column(name = "sla_end_time")
    private LocalDateTime slaEndTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
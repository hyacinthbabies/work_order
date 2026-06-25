package com.njbank.ticket.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ticket_id")
    private Long ticketId;
    
    private String action;
    
    private String content;
    
    @Column(name = "operator_name")
    private String operatorName;
    
    @Column(name = "operator_role")
    private String operatorRole;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
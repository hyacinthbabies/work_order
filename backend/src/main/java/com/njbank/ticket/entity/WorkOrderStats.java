package com.njbank.ticket.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "work_order_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "ticket_type", nullable = false)
    private String ticketType;
    
    @Column(name = "handle_count")
    private Integer handleCount = 0;
    
    @Column(name = "avg_handle_time")
    private Double avgHandleTime;
    
    @Column(name = "praise_rate")
    private Double praiseRate = 0.0;
}
package com.njbank.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private Long id;
    private String ticketNo;
    private String title;
    private String description;
    private String type;
    private String urgency;
    private String channel;
    private String department;
    private String status;
    private String customerNo;
    private String customerName;
    private String phone;
    private String accountNo;
    private String createdBy;
    private String assignedTo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String slaRemaining;
}
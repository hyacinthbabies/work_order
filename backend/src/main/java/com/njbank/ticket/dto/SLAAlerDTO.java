package com.njbank.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAAlerDTO {
    private Long ticketId;
    private String ticketNo;
    private String title;
    private String customerName;
    private String urgency;
    private String slaRemaining;
    private String status;
}
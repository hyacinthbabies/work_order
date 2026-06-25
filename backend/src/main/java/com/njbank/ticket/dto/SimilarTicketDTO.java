package com.njbank.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarTicketDTO {
    private Long id;
    private String ticketNo;
    private String title;
    private String type;
    private String status;
    private Double similarity;
    private String solution;
}

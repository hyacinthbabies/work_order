package com.njbank.ticket.service;

import com.njbank.ticket.dto.TicketDTO;

public interface AssignmentService {
    String assignTicket(Long ticketId);
    String rejectTicket(Long ticketId, String reason);
    String transferTicket(Long ticketId, String newAssignee);
    void handleTimeout(Long ticketId);
}
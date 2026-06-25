package com.njbank.ticket.service;

import com.njbank.ticket.dto.TicketCreateRequest;
import com.njbank.ticket.dto.TicketDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {
    Page<TicketDTO> getTickets(Pageable pageable);
    TicketDTO getTicketById(Long id);
    TicketDTO createTicket(TicketCreateRequest request);
    TicketDTO updateTicket(Long id, TicketCreateRequest request);
    void deleteTicket(Long id);
}
package com.njbank.ticket.controller;

import com.njbank.ticket.dto.ApiResponse;
import com.njbank.ticket.dto.TicketCreateRequest;
import com.njbank.ticket.dto.TicketDTO;
import com.njbank.ticket.service.TicketService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    
    private final TicketService ticketService;
    
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    
    @GetMapping
    public ApiResponse<Page<TicketDTO>> getTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<TicketDTO> tickets = ticketService.getTickets(pageable);
        return ApiResponse.success(tickets);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<TicketDTO> getTicket(@PathVariable Long id) {
        TicketDTO ticket = ticketService.getTicketById(id);
        return ApiResponse.success(ticket);
    }
    
    @PostMapping
    public ApiResponse<TicketDTO> createTicket(@Valid @RequestBody TicketCreateRequest request) {
        TicketDTO ticket = ticketService.createTicket(request);
        return ApiResponse.success("工单创建成功", ticket);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<TicketDTO> updateTicket(@PathVariable Long id, @Valid @RequestBody TicketCreateRequest request) {
        TicketDTO ticket = ticketService.updateTicket(id, request);
        return ApiResponse.success("工单更新成功", ticket);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ApiResponse.success("工单删除成功", null);
    }
}
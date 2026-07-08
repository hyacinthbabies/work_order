package com.njbank.ticket.controller;

import com.njbank.ticket.dto.ApiResponse;
import com.njbank.ticket.dto.TicketCreateRequest;
import com.njbank.ticket.dto.TicketDTO;
import com.njbank.ticket.service.AttachmentService;
import com.njbank.ticket.service.TicketService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    
    private final TicketService ticketService;
    private final AttachmentService attachmentService;
    
    public TicketController(TicketService ticketService, AttachmentService attachmentService) {
        this.ticketService = ticketService;
        this.attachmentService = attachmentService;
    }
    
    @GetMapping
    public ApiResponse<Page<TicketDTO>> getTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String department) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<TicketDTO> tickets = ticketService.searchTickets(keyword, status, type, channel, department, pageable);
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
    
    @PostMapping(value = "/with-attachments", consumes = "multipart/form-data")
    public ApiResponse<TicketDTO> createTicketWithAttachments(
            @RequestParam("title") String title,
            @RequestParam("type") String type,
            @RequestParam("urgency") String urgency,
            @RequestParam(value = "channel", required = false) String channel,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "customerNo", required = false) String customerNo,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "accountNo", required = false) String accountNo,
            @RequestParam("description") String description,
            @RequestParam(value = "files", required = false) MultipartFile[] files) throws IOException {
        
        TicketCreateRequest request = new TicketCreateRequest();
        request.setTitle(title);
        request.setType(type);
        request.setUrgency(urgency);
        request.setChannel(channel);
        request.setDepartment(department);
        request.setCustomerNo(customerNo);
        request.setCustomerName(customerName);
        request.setPhone(phone);
        request.setAccountNo(accountNo);
        request.setDescription(description);
        
        TicketDTO ticket = ticketService.createTicket(request);
        
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    attachmentService.uploadAttachment(ticket.getId(), file);
                }
            }
        }
        
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
    
    @PutMapping("/{id}/revoke")
    public ApiResponse<TicketDTO> revokeTicket(@PathVariable Long id, @RequestBody RevokeRequest request) {
        TicketDTO ticket = ticketService.revokeTicket(id, request.getReason());
        return ApiResponse.success("工单已撤销", ticket);
    }
    
    public static class RevokeRequest {
        private String reason;
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
package com.njbank.ticket.controller;

import com.njbank.ticket.dto.ApiResponse;
import com.njbank.ticket.service.AssignmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignment")
public class AssignmentController {
    
    private final AssignmentService assignmentService;
    
    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }
    
    @PostMapping("/{ticketId}/assign")
    public ApiResponse<String> assignTicket(@PathVariable Long ticketId) {
        String assignee = assignmentService.assignTicket(ticketId);
        return ApiResponse.success("派单成功", assignee);
    }
    
    @PostMapping("/{ticketId}/reject")
    public ApiResponse<String> rejectTicket(@PathVariable Long ticketId, @RequestParam String reason) {
        String newAssignee = assignmentService.rejectTicket(ticketId, reason);
        return ApiResponse.success("拒绝成功，已重新派单", newAssignee);
    }
    
    @PostMapping("/{ticketId}/transfer")
    public ApiResponse<String> transferTicket(@PathVariable Long ticketId, @RequestParam String newAssignee) {
        String assignee = assignmentService.transferTicket(ticketId, newAssignee);
        return ApiResponse.success("转派成功", assignee);
    }
    
    @PostMapping("/{ticketId}/timeout")
    public ApiResponse<Void> handleTimeout(@PathVariable Long ticketId) {
        assignmentService.handleTimeout(ticketId);
        return ApiResponse.success("超时处理完成", null);
    }
}
package com.njbank.ticket.controller;

import com.njbank.ticket.dto.ApiResponse;
import com.njbank.ticket.entity.TicketLog;
import com.njbank.ticket.service.TicketLogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ticket-logs")
public class TicketLogController {

    private final TicketLogService ticketLogService;

    public TicketLogController(TicketLogService ticketLogService) {
        this.ticketLogService = ticketLogService;
    }

    @GetMapping("/ticket/{ticketId}")
    public ApiResponse<List<TicketLog>> getLogsByTicketId(@PathVariable Long ticketId) {
        List<TicketLog> logs = ticketLogService.getLogsByTicketId(ticketId);
        return ApiResponse.success(logs);
    }

    @PostMapping
    public ApiResponse<TicketLog> addLog(@RequestBody Map<String, String> request) {
        Long ticketId = Long.parseLong(request.get("ticketId"));
        String action = request.get("action");
        String content = request.get("content");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String operatorName = authentication != null && authentication.getPrincipal() != null
                ? authentication.getPrincipal().toString() : "未知用户";
        String operatorRole = "一线客服";

        TicketLog log = ticketLogService.addLog(ticketId, action, content, operatorName, operatorRole);
        return ApiResponse.success("处理记录已添加", log);
    }
}
package com.njbank.ticket.service;

import com.njbank.ticket.entity.TicketLog;

import java.util.List;

public interface TicketLogService {
    List<TicketLog> getLogsByTicketId(Long ticketId);
    TicketLog addLog(Long ticketId, String action, String content, String operatorName, String operatorRole);
}
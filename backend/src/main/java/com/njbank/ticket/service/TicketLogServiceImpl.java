package com.njbank.ticket.service;

import com.njbank.ticket.entity.TicketLog;
import com.njbank.ticket.repository.TicketLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketLogServiceImpl implements TicketLogService {

    private final TicketLogRepository ticketLogRepository;

    public TicketLogServiceImpl(TicketLogRepository ticketLogRepository) {
        this.ticketLogRepository = ticketLogRepository;
    }

    @Override
    public List<TicketLog> getLogsByTicketId(Long ticketId) {
        return ticketLogRepository.findByTicketIdOrderByCreateTimeDesc(ticketId);
    }

    @Override
    public TicketLog addLog(Long ticketId, String action, String content, String operatorName, String operatorRole) {
        TicketLog log = TicketLog.builder()
                .ticketId(ticketId)
                .action(action)
                .content(content)
                .operatorName(operatorName)
                .operatorRole(operatorRole)
                .build();
        return ticketLogRepository.save(log);
    }
}
package com.njbank.ticket.repository;

import com.njbank.ticket.entity.TicketLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketLogRepository extends JpaRepository<TicketLog, Long> {
    List<TicketLog> findByTicketIdOrderByCreateTimeDesc(Long ticketId);
}
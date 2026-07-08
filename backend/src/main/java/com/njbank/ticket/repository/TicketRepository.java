package com.njbank.ticket.repository;

import com.njbank.ticket.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Page<Ticket> findByStatus(String status, Pageable pageable);
    Page<Ticket> findByAssignedTo(String assignedTo, Pageable pageable);
    Page<Ticket> findByType(String type, Pageable pageable);
    Page<Ticket> findByChannel(String channel, Pageable pageable);
    List<Ticket> findByStatusOrderByCreateTimeDesc(String status);
    Ticket findByTicketNo(String ticketNo);
}
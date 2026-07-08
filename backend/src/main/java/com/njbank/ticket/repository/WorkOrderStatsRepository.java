package com.njbank.ticket.repository;

import com.njbank.ticket.entity.WorkOrderStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderStatsRepository extends JpaRepository<WorkOrderStats, Long> {
    Optional<WorkOrderStats> findByUsernameAndTicketType(String username, String ticketType);
    List<WorkOrderStats> findByUsername(String username);
}
package com.njbank.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private Integer totalTickets;
    private Integer pendingTickets;
    private Integer processingTickets;
    private Integer completedTickets;
    private Integer timeoutTickets;
    private Integer highUrgencyTickets;
    private Integer mediumUrgencyTickets;
    private Integer lowUrgencyTickets;
    private Integer todayNewTickets;
    private Integer todayProcessedTickets;
    private List<SLAAlerDTO> slaAlerts;
    private List<TicketDTO> recentTickets;
    private List<TicketDTO> myTasks;
    private Integer myTasksCount;
    private Double aiAssignAccuracy;
    private Double aiClassifyAccuracy;
    private Double aiHandleRate;
    private List<HotIssueDTO> hotIssues;
    private List<DailyStatsDTO> dailyStats;
}
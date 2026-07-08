package com.njbank.ticket.service;

import com.njbank.ticket.dto.*;
import com.njbank.ticket.entity.Ticket;
import com.njbank.ticket.repository.TicketRepository;
import com.njbank.ticket.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {
    
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    
    private static final int MY_TASKS_LIMIT = 4;
    private static final int HOT_ISSUES_LIMIT = 5;
    
    public DashboardServiceImpl(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public DashboardDTO getDashboardData(String username, String role, String department) {
        List<Ticket> tickets = getTicketsForUser(username, role, department);
        List<Ticket> allTickets = ticketRepository.findAll();
        
        int totalTickets = tickets.size();
        int pendingTickets = 0;
        int processingTickets = 0;
        int completedTickets = 0;
        int timeoutTickets = 0;
        int highUrgencyTickets = 0;
        int mediumUrgencyTickets = 0;
        int lowUrgencyTickets = 0;
        
        for (Ticket ticket : tickets) {
            String status = ticket.getStatus();
            String urgency = ticket.getUrgency();
            
            if ("待派单".equals(status) || "待处理".equals(status)) {
                pendingTickets++;
            } else if ("处理中".equals(status)) {
                processingTickets++;
            } else if ("已完成".equals(status) || "已结案".equals(status)) {
                completedTickets++;
            }
            
            if (isTimeout(ticket)) {
                timeoutTickets++;
            }
            
            if ("high".equals(urgency)) {
                highUrgencyTickets++;
            } else if ("medium".equals(urgency)) {
                mediumUrgencyTickets++;
            } else if ("low".equals(urgency)) {
                lowUrgencyTickets++;
            }
        }
        
        int todayNewTickets = countTodayNewTicketsForUser(allTickets, username);
        int todayProcessedTickets = countTodayProcessedTickets(tickets);
        
        List<SLAAlerDTO> slaAlerts = getSLAAlersts(tickets);
        List<TicketDTO> recentTickets = getRecentTickets(tickets);
        List<TicketDTO> myTasks = getMyTasks(username);
        int myTasksCount = getMyTasksCount(username);
        
        Double aiAssignAccuracy = calculateAIAssignAccuracy(allTickets);
        Double aiClassifyAccuracy = calculateAIClassifyAccuracy(allTickets);
        Double aiHandleRate = calculateAIHandleRate(allTickets);
        
        List<HotIssueDTO> hotIssues = getHotIssues(allTickets);
        List<DailyStatsDTO> dailyStats = getDailyStats(allTickets);
        
        return DashboardDTO.builder()
                .totalTickets(totalTickets)
                .pendingTickets(pendingTickets)
                .processingTickets(processingTickets)
                .completedTickets(completedTickets)
                .timeoutTickets(timeoutTickets)
                .highUrgencyTickets(highUrgencyTickets)
                .mediumUrgencyTickets(mediumUrgencyTickets)
                .lowUrgencyTickets(lowUrgencyTickets)
                .todayNewTickets(todayNewTickets)
                .todayProcessedTickets(todayProcessedTickets)
                .slaAlerts(slaAlerts)
                .recentTickets(recentTickets)
                .myTasks(myTasks)
                .myTasksCount(myTasksCount)
                .aiAssignAccuracy(aiAssignAccuracy)
                .aiClassifyAccuracy(aiClassifyAccuracy)
                .aiHandleRate(aiHandleRate)
                .hotIssues(hotIssues)
                .dailyStats(dailyStats)
                .build();
    }
    
    private List<Ticket> getTicketsForUser(String username, String role, String department) {
        List<Ticket> allTickets = ticketRepository.findAll();
        List<Ticket> result = new ArrayList<>();
        
        if (role == null) {
            for (Ticket t : allTickets) {
                if (username.equals(t.getAssignedTo()) || username.equals(t.getCreatedBy())) {
                    result.add(t);
                }
            }
            return result;
        }
        
        switch (role) {
            case "系统管理员":
                return allTickets;
            case "客服主管":
                if (department != null) {
                    for (Ticket t : allTickets) {
                        if (department.equals(t.getDepartment())) {
                            result.add(t);
                        }
                    }
                    return result;
                }
                return allTickets;
            case "高级工程师":
                for (Ticket t : allTickets) {
                    boolean match = username.equals(t.getAssignedTo()) 
                            || username.equals(t.getCreatedBy());
                    if (!match && department != null) {
                        match = department.equals(t.getDepartment()) && "待派单".equals(t.getStatus());
                    }
                    if (match) {
                        result.add(t);
                    }
                }
                return result;
            case "一线客服":
                for (Ticket t : allTickets) {
                    boolean match = username.equals(t.getAssignedTo()) 
                            || username.equals(t.getCreatedBy());
                    if (!match && department != null) {
                        match = department.equals(t.getDepartment()) && "待派单".equals(t.getStatus());
                    }
                    if (match) {
                        result.add(t);
                    }
                }
                return result;
            default:
                for (Ticket t : allTickets) {
                    if (username.equals(t.getAssignedTo()) || username.equals(t.getCreatedBy())) {
                        result.add(t);
                    }
                }
                return result;
        }
    }
    
    private int countTodayNewTickets(List<Ticket> tickets) {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Ticket t : tickets) {
            if (t.getCreateTime() != null && t.getCreateTime().toLocalDate().equals(today)) {
                count++;
            }
        }
        return count;
    }
    
    public int countTodayNewTicketsForUser(List<Ticket> allTickets, String username) {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Ticket t : allTickets) {
            if (username.equals(t.getCreatedBy()) || username.equals(t.getAssignedTo())) {
                if (t.getCreateTime() != null && t.getCreateTime().toLocalDate().equals(today)) {
                    count++;
                }
            }
        }
        return count;
    }
    
    private int countTodayProcessedTickets(List<Ticket> tickets) {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Ticket t : tickets) {
            String status = t.getStatus();
            if (("已完成".equals(status) || "已结案".equals(status)) 
                    && t.getUpdateTime() != null 
                    && t.getUpdateTime().toLocalDate().equals(today)) {
                count++;
            }
        }
        return count;
    }
    
    private List<TicketDTO> getMyTasks(String username) {
        List<Ticket> allTickets = ticketRepository.findAll();
        List<TicketDTO> result = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();
        
        List<Ticket> sortedList = new ArrayList<>();
        for (Ticket t : allTickets) {
            if (username.equals(t.getAssignedTo()) || username.equals(t.getCreatedBy())) {
                String status = t.getStatus();
                if (!"已完成".equals(status) && !"已结案".equals(status) && !"已撤销".equals(status)) {
                    sortedList.add(t);
                }
            }
        }
        
        sortedList.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        
        for (Ticket t : sortedList) {
            if (!seenIds.contains(t.getId()) && result.size() < MY_TASKS_LIMIT) {
                seenIds.add(t.getId());
                result.add(convertToDTO(t));
            }
        }
        
        return result;
    }
    
    private int getMyTasksCount(String username) {
        List<Ticket> allTickets = ticketRepository.findAll();
        int count = 0;
        for (Ticket t : allTickets) {
            if (username.equals(t.getAssignedTo()) || username.equals(t.getCreatedBy())) {
                String status = t.getStatus();
                if (!"已完成".equals(status) && !"已结案".equals(status) && !"已撤销".equals(status)) {
                    count++;
                }
            }
        }
        return count;
    }
    
    private boolean isTimeout(Ticket ticket) {
        if (ticket.getSlaEndTime() == null) {
            return false;
        }
        String status = ticket.getStatus();
        return ticket.getSlaEndTime().isBefore(LocalDateTime.now()) 
                && !"已完成".equals(status) 
                && !"已结案".equals(status)
                && !"已撤销".equals(status);
    }
    
    private List<SLAAlerDTO> getSLAAlersts(List<Ticket> tickets) {
        List<SLAAlerDTO> result = new ArrayList<>();
        
        for (Ticket t : tickets) {
            if (t.getSlaEndTime() == null) {
                continue;
            }
            
            boolean isTimeout = isTimeout(t);
            if (!isTimeout) {
                Duration remaining = Duration.between(LocalDateTime.now(), t.getSlaEndTime());
                if (remaining.toHours() > 1) {
                    continue;
                }
            }
            
            String slaRemaining;
            Duration duration = Duration.between(LocalDateTime.now(), t.getSlaEndTime());
            if (duration.isNegative()) {
                slaRemaining = "已超时 " + Math.abs(duration.toHours()) + "小时" + Math.abs(duration.toMinutes() % 60) + "分钟";
            } else {
                slaRemaining = duration.toHours() + "小时" + duration.toMinutes() % 60 + "分钟";
            }
            
            SLAAlerDTO alert = SLAAlerDTO.builder()
                    .ticketId(t.getId())
                    .ticketNo(t.getTicketNo())
                    .title(t.getTitle())
                    .customerName(t.getCustomerName())
                    .urgency(t.getUrgency())
                    .slaRemaining(slaRemaining)
                    .status(t.getStatus())
                    .build();
            
            if (slaRemaining.startsWith("已超时")) {
                int insertIndex = 0;
                while (insertIndex < result.size()) {
                    if (!result.get(insertIndex).getSlaRemaining().startsWith("已超时")) {
                        break;
                    }
                    insertIndex++;
                }
                result.add(insertIndex, alert);
            } else {
                result.add(alert);
            }
        }
        
        if (result.size() > 10) {
            result = result.subList(0, 10);
        }
        
        return result;
    }
    
    private List<TicketDTO> getRecentTickets(List<Ticket> tickets) {
        List<Ticket> sortedList = new ArrayList<>(tickets);
        sortedList.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        
        List<TicketDTO> result = new ArrayList<>();
        int limit = Math.min(10, sortedList.size());
        for (int i = 0; i < limit; i++) {
            result.add(convertToDTO(sortedList.get(i)));
        }
        
        return result;
    }
    
    private Double calculateAIAssignAccuracy(List<Ticket> tickets) {
        long total = 0;
        long accepted = 0;
        
        for (Ticket t : tickets) {
            String status = t.getStatus();
            if (("已完成".equals(status) || "已结案".equals(status)) && t.getAssignedTo() != null) {
                total++;
                if (!t.getCreatedBy().equals(t.getAssignedTo())) {
                    accepted++;
                }
            }
        }
        
        if (total == 0) return 0.0;
        return Math.round(accepted * 1000.0 / total) / 10.0;
    }
    
    private Double calculateAIClassifyAccuracy(List<Ticket> tickets) {
        if (tickets.size() == 0) return 0.0;
        
        long validType = 0;
        for (Ticket t : tickets) {
            if (t.getType() != null && !t.getType().isEmpty()) {
                validType++;
            }
        }
        
        return Math.round(validType * 1000.0 / tickets.size()) / 10.0;
    }
    
    private Double calculateAIHandleRate(List<Ticket> tickets) {
        if (tickets.size() == 0) return 0.0;
        
        long autoAssigned = 0;
        for (Ticket t : tickets) {
            if (t.getAssignedTo() != null && !t.getCreatedBy().equals(t.getAssignedTo())) {
                autoAssigned++;
            }
        }
        
        return Math.round(autoAssigned * 1000.0 / tickets.size()) / 10.0;
    }
    
    private List<HotIssueDTO> getHotIssues(List<Ticket> tickets) {
        Map<String, Integer> issueCounts = new HashMap<>();
        
        String[] keywords = {"转账", "账单", "账户", "贷款", "理财", "登录", "挂失", "投诉", "密码", "短信", "交易", "余额"};
        
        for (Ticket t : tickets) {
            String title = t.getTitle();
            if (title == null || title.isEmpty()) continue;
            
            String matchedKeyword = null;
            for (String keyword : keywords) {
                if (title.contains(keyword)) {
                    matchedKeyword = getIssueTitle(keyword);
                    break;
                }
            }
            
            if (matchedKeyword != null) {
                issueCounts.put(matchedKeyword, issueCounts.getOrDefault(matchedKeyword, 0) + 1);
            } else {
                issueCounts.put(title, issueCounts.getOrDefault(title, 0) + 1);
            }
        }
        
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(issueCounts.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        List<HotIssueDTO> result = new ArrayList<>();
        int limit = Math.min(HOT_ISSUES_LIMIT, sortedEntries.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = sortedEntries.get(i);
            result.add(HotIssueDTO.builder()
                    .title(entry.getKey())
                    .count(entry.getValue())
                    .build());
        }
        
        return result;
    }
    
    private String getIssueTitle(String keyword) {
        Map<String, String> keywordMap = new HashMap<>();
        keywordMap.put("转账", "转账问题");
        keywordMap.put("账单", "账单疑问");
        keywordMap.put("账户", "账户问题");
        keywordMap.put("贷款", "贷款咨询");
        keywordMap.put("理财", "理财产品");
        keywordMap.put("登录", "登录问题");
        keywordMap.put("挂失", "挂失办理");
        keywordMap.put("投诉", "投诉处理");
        keywordMap.put("密码", "密码重置");
        keywordMap.put("短信", "短信通知");
        keywordMap.put("交易", "交易异常");
        keywordMap.put("余额", "余额查询");
        return keywordMap.getOrDefault(keyword, keyword + "相关");
    }
    
    private List<DailyStatsDTO> getDailyStats(List<Ticket> tickets) {
        List<DailyStatsDTO> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(formatter);
            
            int count = 0;
            for (Ticket t : tickets) {
                if (t.getCreateTime() != null && t.getCreateTime().toLocalDate().equals(date)) {
                    count++;
                }
            }
            
            result.add(DailyStatsDTO.builder()
                    .date(dateStr)
                    .count(count)
                    .build());
        }
        
        return result;
    }
    
    private TicketDTO convertToDTO(Ticket ticket) {
        String slaRemaining = null;
        if (ticket.getSlaEndTime() != null) {
            Duration duration = Duration.between(LocalDateTime.now(), ticket.getSlaEndTime());
            if (duration.isNegative()) {
                slaRemaining = "已超时";
            } else {
                slaRemaining = String.format("%d小时%d分钟", duration.toHours(), duration.toMinutes() % 60);
            }
        }
        
        return TicketDTO.builder()
                .id(ticket.getId())
                .ticketNo(ticket.getTicketNo())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .type(ticket.getType())
                .urgency(ticket.getUrgency())
                .channel(ticket.getChannel())
                .department(ticket.getDepartment())
                .status(ticket.getStatus())
                .customerNo(ticket.getCustomerNo())
                .customerName(ticket.getCustomerName())
                .phone(ticket.getPhone())
                .accountNo(ticket.getAccountNo())
                .createdBy(ticket.getCreatedBy())
                .assignedTo(ticket.getAssignedTo())
                .createTime(ticket.getCreateTime())
                .updateTime(ticket.getUpdateTime())
                .slaRemaining(slaRemaining)
                .build();
    }
}
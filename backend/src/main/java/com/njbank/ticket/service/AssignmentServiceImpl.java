package com.njbank.ticket.service;

import com.njbank.ticket.entity.*;
import com.njbank.ticket.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssignmentServiceImpl implements AssignmentService {
    
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CustomerAssigneeRepository customerAssigneeRepository;
    private final WorkOrderStatsRepository workOrderStatsRepository;
    private final TicketLogRepository ticketLogRepository;

    private static final double WEIGHT_BUSINESS = 0.35;
    private static final double WEIGHT_LOAD = 0.25;
    private static final double WEIGHT_EXPERIENCE = 0.20;
    private static final double WEIGHT_URGENT = 0.15;
    private static final double WEIGHT_CONTINUITY = 0.05;

    public AssignmentServiceImpl(TicketRepository ticketRepository, 
                                  UserRepository userRepository,
                                  CustomerAssigneeRepository customerAssigneeRepository,
                                  WorkOrderStatsRepository workOrderStatsRepository,
                                  TicketLogRepository ticketLogRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.customerAssigneeRepository = customerAssigneeRepository;
        this.workOrderStatsRepository = workOrderStatsRepository;
        this.ticketLogRepository = ticketLogRepository;
    }

    @Override
    @Transactional
    public String assignTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("工单不存在"));

        List<User> candidates = getCandidates(ticket);
        
        if (candidates.isEmpty()) {
            return null;
        }

        String bestAssignee = calculateBestAssignee(ticket, candidates);
        
        if (bestAssignee != null) {
            ticket.setAssignedTo(bestAssignee);
            ticket.setStatus("处理中");
            ticketRepository.save(ticket);

            saveLog(ticketId, "系统派单", "自动派单给: " + bestAssignee);

            updateCustomerAssignee(ticket.getCustomerNo(), bestAssignee);
        }
        
        return bestAssignee;
    }

    private List<User> getCandidates(Ticket ticket) {
        return userRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .filter(u -> u.getRole() != null && 
                           (u.getRole().equals("一线客服") || 
                            u.getRole().equals("高级工程师") || 
                            u.getRole().equals("客服主管")))
                .collect(Collectors.toList());
    }

    private String calculateBestAssignee(Ticket ticket, List<User> candidates) {
        double highestScore = -1;
        String bestAssignee = null;

        for (User candidate : candidates) {
            double score = calculate综合评分(ticket, candidate);
            
            if (score > highestScore) {
                highestScore = score;
                bestAssignee = candidate.getUsername();
            }
        }

        return bestAssignee;
    }

    private double calculate综合评分(Ticket ticket, User user) {
        double sBusiness = calculateSBusiness(ticket, user);
        double sLoad = calculateSLoad(user);
        double sExperience = calculateSExperience(ticket, user);
        double sUrgent = calculateSUrgent(ticket, user);
        double sContinuity = calculateSContinuity(ticket, user);

        return WEIGHT_BUSINESS * sBusiness +
               WEIGHT_LOAD * sLoad +
               WEIGHT_EXPERIENCE * sExperience +
               WEIGHT_URGENT * sUrgent +
               WEIGHT_CONTINUITY * sContinuity;
    }

    private double calculateSBusiness(Ticket ticket, User user) {
        Map<String, List<String>> typeSkillMap = new HashMap<>();
        typeSkillMap.put("咨询类", Arrays.asList("账户查询", "账单疑问", "业务咨询"));
        typeSkillMap.put("办理类", Arrays.asList("挂失", "开户", "转账", "理财"));
        typeSkillMap.put("投诉类", Arrays.asList("投诉处理", "服务态度"));
        typeSkillMap.put("风控类", Arrays.asList("风险识别", "异常交易"));

        List<String> requiredSkills = typeSkillMap.getOrDefault(ticket.getType(), Collections.emptyList());
        
        if (requiredSkills.isEmpty()) {
            return 50.0;
        }

        List<String> userSkills = user.getSkillTags() != null 
                ? Arrays.asList(user.getSkillTags().split(","))
                : Collections.emptyList();

        long matchCount = userSkills.stream()
                .filter(skill -> requiredSkills.stream().anyMatch(req -> req.contains(skill) || skill.contains(req)))
                .count();

        return (double) matchCount / requiredSkills.size() * 100;
    }

    private double calculateSLoad(User user) {
        long currentCount = ticketRepository.findAll().stream()
                .filter(t -> user.getUsername().equals(t.getAssignedTo()))
                .filter(t -> !"已完成".equals(t.getStatus()) && !"已撤销".equals(t.getStatus()))
                .count();

        long timeoutCount = ticketRepository.findAll().stream()
                .filter(t -> user.getUsername().equals(t.getAssignedTo()))
                .filter(t -> t.getSlaEndTime() != null && t.getSlaEndTime().isBefore(LocalDateTime.now()))
                .filter(t -> !"已完成".equals(t.getStatus()) && !"已撤销".equals(t.getStatus()))
                .count();

        double score = 100 - currentCount * 10 - timeoutCount * 20;
        return Math.max(0, score);
    }

    private double calculateSExperience(Ticket ticket, User user) {
        Optional<WorkOrderStats> statsOpt = workOrderStatsRepository
                .findByUsernameAndTicketType(user.getUsername(), ticket.getType());

        if (statsOpt.isPresent()) {
            WorkOrderStats stats = statsOpt.get();
            double score = stats.getHandleCount() * 5 + (stats.getPraiseRate() != null ? stats.getPraiseRate() * 100 : 0);
            return Math.min(100, Math.max(0, score));
        }
        
        return 30.0;
    }

    private double calculateSUrgent(Ticket ticket, User user) {
        if (Boolean.TRUE.equals(user.getIsOnDuty())) {
            return 100.0;
        }
        
        if ("高级工程师".equals(user.getEngineerLevel())) {
            return 60.0;
        }
        
        return 30.0;
    }

    private double calculateSContinuity(Ticket ticket, User user) {
        if (ticket.getCustomerNo() == null) {
            return 0.0;
        }

        Optional<CustomerAssignee> caOpt = customerAssigneeRepository.findByCustomerNo(ticket.getCustomerNo());
        
        if (caOpt.isPresent()) {
            if (user.getUsername().equals(caOpt.get().getAssigneeUsername())) {
                return 100.0;
            }
        }

        List<User> sameDeptUsers = userRepository.findAll().stream()
                .filter(u -> user.getDepartment() != null && user.getDepartment().equals(u.getDepartment()))
                .collect(Collectors.toList());
        
        if (sameDeptUsers.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            return 50.0;
        }

        return 0.0;
    }

    @Override
    @Transactional
    public String rejectTicket(Long ticketId, String reason) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("工单不存在"));

        String rejecter = ticket.getAssignedTo();
        ticket.setStatus("待派单");
        ticket.setAssignedTo(null);
        ticketRepository.save(ticket);

        saveLog(ticketId, "处理人拒绝", "处理人: " + rejecter + ", 原因: " + reason);

        decreaseExperienceScore(rejecter, ticket.getType());

        assignTicket(ticketId);

        return ticket.getAssignedTo();
    }

    @Override
    @Transactional
    public String transferTicket(Long ticketId, String newAssignee) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("工单不存在"));

        long transferCount = ticketLogRepository.findAll().stream()
                .filter(log -> log.getTicketId().equals(ticketId))
                .filter(log -> "转派".equals(log.getAction()))
                .count();

        if (transferCount >= 2) {
            Optional<User> supervisor = userRepository.findAll().stream()
                    .filter(u -> "客服主管".equals(u.getRole()))
                    .filter(u -> ticket.getDepartment() != null && ticket.getDepartment().equals(u.getDepartment()))
                    .findFirst();

            if (supervisor.isPresent()) {
                newAssignee = supervisor.get().getUsername();
            } else {
                throw new RuntimeException("转派次数已达上限，且未找到部门主管");
            }
        }

        String oldAssignee = ticket.getAssignedTo();
        ticket.setAssignedTo(newAssignee);
        ticket.setStatus("处理中");
        ticketRepository.save(ticket);

        saveLog(ticketId, "转派", "从 " + oldAssignee + " 转派给 " + newAssignee);

        return newAssignee;
    }

    @Override
    @Transactional
    public void handleTimeout(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("工单不存在"));

        ticket.setStatus("待派单");
        ticket.setAssignedTo(null);
        ticketRepository.save(ticket);

        saveLog(ticketId, "SLA超时", "工单超时，自动升级");

        Optional<User> supervisor = userRepository.findAll().stream()
                .filter(u -> "客服主管".equals(u.getRole()))
                .filter(u -> ticket.getDepartment() != null && ticket.getDepartment().equals(u.getDepartment()))
                .findFirst();

        if (supervisor.isPresent()) {
            ticket.setAssignedTo(supervisor.get().getUsername());
            ticket.setStatus("处理中");
            ticketRepository.save(ticket);
            saveLog(ticketId, "主管接管", "主管: " + supervisor.get().getUsername() + " 接管超时工单");
        }
    }

    private void decreaseExperienceScore(String username, String ticketType) {
        Optional<WorkOrderStats> statsOpt = workOrderStatsRepository.findByUsernameAndTicketType(username, ticketType);
        
        if (statsOpt.isPresent()) {
            WorkOrderStats stats = statsOpt.get();
            stats.setPraiseRate(Math.max(0, (stats.getPraiseRate() != null ? stats.getPraiseRate() : 0) - 0.1));
            workOrderStatsRepository.save(stats);
        }
    }

    private void updateCustomerAssignee(String customerNo, String assignee) {
        if (customerNo == null) {
            return;
        }
        
        Optional<CustomerAssignee> caOpt = customerAssigneeRepository.findByCustomerNo(customerNo);
        
        if (caOpt.isPresent()) {
            CustomerAssignee ca = caOpt.get();
            ca.setAssigneeUsername(assignee);
            customerAssigneeRepository.save(ca);
        } else {
            CustomerAssignee ca = CustomerAssignee.builder()
                    .customerNo(customerNo)
                    .assigneeUsername(assignee)
                    .build();
            customerAssigneeRepository.save(ca);
        }
    }

    private void saveLog(Long ticketId, String action, String detail) {
        TicketLog log = TicketLog.builder()
                .ticketId(ticketId)
                .action(action)
                .content(detail)
                .createTime(LocalDateTime.now())
                .build();
        ticketLogRepository.save(log);
    }
}
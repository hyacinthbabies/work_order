package com.njbank.ticket.service;

import com.njbank.ticket.dto.TicketCreateRequest;
import com.njbank.ticket.dto.TicketDTO;
import com.njbank.ticket.entity.Ticket;
import com.njbank.ticket.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class TicketServiceImpl implements TicketService {
    
    private final TicketRepository ticketRepository;
    private final AssignmentService assignmentService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    public TicketServiceImpl(TicketRepository ticketRepository, AssignmentService assignmentService) {
        this.ticketRepository = ticketRepository;
        this.assignmentService = assignmentService;
    }
    
    @Override
    public Page<TicketDTO> getTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(this::convertToDTO);
    }
    
    @Override
    public Page<TicketDTO> searchTickets(String keyword, String status, String type, String channel, String department, Pageable pageable) {
        Specification<Ticket> spec = buildSpecification(keyword, status, type, channel, department);
        return ticketRepository.findAll(spec, pageable).map(this::convertToDTO);
    }
    
    private Specification<Ticket> buildSpecification(String keyword, String status, String type, String channel, String department) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword + "%";
                Predicate ticketNoLike = cb.like(root.get("ticketNo"), likeKeyword);
                Predicate titleLike = cb.like(root.get("title"), likeKeyword);
                Predicate customerNameLike = cb.like(root.get("customerName"), likeKeyword);
                predicates.add(cb.or(ticketNoLike, titleLike, customerNameLike));
            }
            
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            if (StringUtils.hasText(type)) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            
            if (StringUtils.hasText(channel)) {
                predicates.add(cb.equal(root.get("channel"), channel));
            }
            
            if (StringUtils.hasText(department)) {
                predicates.add(cb.equal(root.get("department"), department));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    @Override
    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("工单不存在"));
        return convertToDTO(ticket);
    }
    
    @Override
    public TicketDTO createTicket(TicketCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication != null && authentication.getPrincipal() != null 
                ? authentication.getPrincipal().toString() : "admin";
        
        Ticket ticket = Ticket.builder()
                .ticketNo(generateTicketNo())
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .urgency(request.getUrgency())
                .channel(request.getChannel())
                .department(request.getDepartment())
                .customerNo(request.getCustomerNo())
                .customerName(request.getCustomerName())
                .phone(request.getPhone())
                .accountNo(request.getAccountNo())
                .createdBy(currentUser)
                .status("待派单")
                .build();
        
        int slaHours;
        switch (request.getUrgency()) {
            case "high":
                slaHours = 2;
                break;
            case "medium":
                slaHours = 4;
                break;
            default:
                slaHours = 8;
        }
        ticket.setSlaEndTime(LocalDateTime.now().plusHours(slaHours));
        
        Ticket saved = ticketRepository.save(ticket);
        
        assignmentService.assignTicket(saved.getId());
        
        return convertToDTO(ticketRepository.findById(saved.getId()).orElse(saved));
    }
    
    @Override
    public TicketDTO updateTicket(Long id, TicketCreateRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("工单不存在"));
        
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setType(request.getType());
        ticket.setUrgency(request.getUrgency());
        ticket.setChannel(request.getChannel());
        ticket.setDepartment(request.getDepartment());
        ticket.setCustomerNo(request.getCustomerNo());
        ticket.setCustomerName(request.getCustomerName());
        ticket.setPhone(request.getPhone());
        ticket.setAccountNo(request.getAccountNo());
        
        Ticket updated = ticketRepository.save(ticket);
        return convertToDTO(updated);
    }
    
    @Override
    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new RuntimeException("工单不存在");
        }
        ticketRepository.deleteById(id);
    }
    
    @Override
    public TicketDTO revokeTicket(Long id, String reason) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("工单不存在"));
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("请先登录");
        }
        String currentUser = authentication.getPrincipal().toString();
        
        if (!currentUser.equals(ticket.getCreatedBy())) {
            throw new RuntimeException("只有工单创建人才能撤销工单");
        }
        
        if ("已撤销".equals(ticket.getStatus())) {
            throw new RuntimeException("工单已被撤销");
        }
        
        if ("已结案".equals(ticket.getStatus()) || "已完成".equals(ticket.getStatus())) {
            throw new RuntimeException("已完结的工单不能撤销");
        }
        
        ticket.setStatus("已撤销");
        ticket.setDescription(ticket.getDescription() + "\n\n【撤销原因】：" + reason);
        
        Ticket updated = ticketRepository.save(ticket);
        return convertToDTO(updated);
    }
    
    private String generateTicketNo() {
        return "WO" + LocalDateTime.now().format(formatter) + String.format("%04d", new Random().nextInt(9999));
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
package com.njbank.ticket.service;

import com.njbank.ticket.dto.TicketCreateRequest;
import com.njbank.ticket.dto.TicketDTO;
import com.njbank.ticket.entity.Ticket;
import com.njbank.ticket.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class TicketServiceImpl implements TicketService {
    
    private final TicketRepository ticketRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }
    
    @Override
    public Page<TicketDTO> getTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(this::convertToDTO);
    }
    
    @Override
    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("工单不存在"));
        return convertToDTO(ticket);
    }
    
    @Override
    public TicketDTO createTicket(TicketCreateRequest request) {
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
                .createdBy("admin")
                .status("待处理")
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
        return convertToDTO(saved);
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
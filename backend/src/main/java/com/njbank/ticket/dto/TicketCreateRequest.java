package com.njbank.ticket.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreateRequest {
    @NotBlank(message = "工单标题不能为空")
    private String title;
    
    @NotBlank(message = "工单类型不能为空")
    private String type;
    
    @NotBlank(message = "紧急程度不能为空")
    private String urgency;
    
    private String channel;
    
    private String department;
    
    private String customerNo;
    
    private String customerName;
    
    private String phone;
    
    private String accountNo;
    
    @NotBlank(message = "问题描述不能为空")
    private String description;
}
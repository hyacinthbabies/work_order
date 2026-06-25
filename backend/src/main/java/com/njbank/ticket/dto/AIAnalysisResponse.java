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
public class AIAnalysisResponse {
    private String ticketType;
    private String ticketTypeDetail;
    private String urgency;
    private String department;
    private String channel;
    private String relatedSystem;
    private String solution;
    private String processingTime;
    private Double confidence;
    private String title;
    private String categoryPath;
    
    // 问题分析（智能体对话风格的原因解释）
    private String problemAnalysis;
    
    // 建议操作（简短的行动建议）
    private String suggestedActions;
    
    // 相似工单列表
    private List<SimilarTicketDTO> similarTickets;
    
    // 问题模版字段
    private List<TemplateFieldDTO> templateFields;
}

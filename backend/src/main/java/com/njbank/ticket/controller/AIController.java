package com.njbank.ticket.controller;

import com.njbank.ticket.dto.AIAnalysisResponse;
import com.njbank.ticket.dto.ApiResponse;
import com.njbank.ticket.dto.SimilarTicketDTO;
import com.njbank.ticket.dto.TemplateFieldDTO;
import com.njbank.ticket.service.AIService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze")
    public ApiResponse<AIAnalysisResponse> analyzeTitle(@RequestBody java.util.Map<String, String> request) {
        String title = request.get("title");
        AIAnalysisResponse response = aiService.analyzeTitle(title);
        return ApiResponse.success(response);
    }

    /**
     * SSE流式响应AI分析结果
     */
    @PostMapping(value = "/analyze/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyzeStream(@RequestBody java.util.Map<String, String> request) {
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时
        String title = request.get("title");

        executorService.execute(() -> {
            try {
                // 1. 发送开始事件
                emitter.send(SseEmitter.event().name("start").data("开始分析..."));
                Thread.sleep(200);

                // 2. 分析并流式发送问题类型
                AIAnalysisResponse response = aiService.analyzeTitle(title);
                
                // 发送分类路径
                if (response.getCategoryPath() != null) {
                    emitter.send(SseEmitter.event().name("categoryPath").data(response.getCategoryPath()));
                    Thread.sleep(150);
                }

                // 发送工单类型
                if (response.getTicketType() != null) {
                    emitter.send(SseEmitter.event().name("ticketType").data(response.getTicketType()));
                    Thread.sleep(150);
                }

                // 发送紧急程度
                if (response.getUrgency() != null) {
                    emitter.send(SseEmitter.event().name("urgency").data(response.getUrgency()));
                    Thread.sleep(150);
                }

                // 发送处理部门
                if (response.getDepartment() != null) {
                    emitter.send(SseEmitter.event().name("department").data(response.getDepartment()));
                    Thread.sleep(150);
                }

                // 发送置信度
                if (response.getConfidence() != null) {
                    emitter.send(SseEmitter.event().name("confidence").data(response.getConfidence()));
                    Thread.sleep(200);
                }

                // 发送问题分析（智能体对话风格的原因解释）
                if (response.getProblemAnalysis() != null) {
                    emitter.send(SseEmitter.event().name("problemAnalysis").data(response.getProblemAnalysis()));
                    Thread.sleep(200);
                }

                // 发送建议操作
                if (response.getSuggestedActions() != null) {
                    emitter.send(SseEmitter.event().name("suggestedActions").data(response.getSuggestedActions()));
                    Thread.sleep(200);
                }

                // 3. 发送推荐方案
                if (response.getSolution() != null) {
                    // 逐行发送方案
                    String[] solutionLines = response.getSolution().split("\n");
                    StringBuilder solutionBuilder = new StringBuilder();
                    for (String line : solutionLines) {
                        solutionBuilder.append(line).append("\n");
                        emitter.send(SseEmitter.event().name("solution").data(solutionBuilder.toString()));
                        Thread.sleep(100);
                    }
                }

                // 4. 发送相似工单
                if (response.getSimilarTickets() != null && !response.getSimilarTickets().isEmpty()) {
                    int index = 0;
                    for (SimilarTicketDTO ticket : response.getSimilarTickets()) {
                        emitter.send(SseEmitter.event().name("similarTicket").data(
                                ticket.getId() + "|" + ticket.getTicketNo() + "|" + ticket.getTitle() + "|" + 
                                String.format("%.2f", ticket.getSimilarity() * 100) + "|" + ticket.getType()
                        ));
                        Thread.sleep(100);
                        index++;
                    }
                }

                // 5. 发送问题模版
                if (response.getTemplateFields() != null && !response.getTemplateFields().isEmpty()) {
                    for (TemplateFieldDTO field : response.getTemplateFields()) {
                        emitter.send(SseEmitter.event().name("templateField").data(
                                field.getFieldName() + "|" + field.getPlaceholder() + "|" + field.getRequired()
                        ));
                        Thread.sleep(100);
                    }
                }

                // 6. 发送完成事件
                emitter.send(SseEmitter.event().name("complete").data("分析完成"));
                emitter.complete();

            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}

package com.njbank.ticket.controller;

import com.njbank.ticket.dto.ApiResponse;
import com.njbank.ticket.entity.KnowledgeKeyword;
import com.njbank.ticket.entity.KnowledgeSolution;
import com.njbank.ticket.service.KnowledgeBaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping("/keywords")
    public ApiResponse<List<KnowledgeKeyword>> getAllKeywords() {
        return ApiResponse.success(knowledgeBaseService.getAllKeywords());
    }

    @GetMapping("/keywords/category/{category}")
    public ApiResponse<List<KnowledgeKeyword>> getKeywordsByCategory(@PathVariable String category) {
        return ApiResponse.success(knowledgeBaseService.getKeywordsByCategory(category));
    }

    @PostMapping("/keywords")
    public ApiResponse<KnowledgeKeyword> saveKeyword(@RequestBody KnowledgeKeyword keyword) {
        return ApiResponse.success(knowledgeBaseService.saveKeyword(keyword));
    }

    @DeleteMapping("/keywords/{id}")
    public ApiResponse<Void> deleteKeyword(@PathVariable Long id) {
        knowledgeBaseService.deleteKeyword(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/solutions")
    public ApiResponse<List<KnowledgeSolution>> getAllSolutions() {
        return ApiResponse.success(knowledgeBaseService.getAllSolutions());
    }

    @GetMapping("/solutions/category/{category}")
    public ApiResponse<List<KnowledgeSolution>> getSolutionsByCategory(@PathVariable String category) {
        return ApiResponse.success(knowledgeBaseService.getSolutionsByCategory(category));
    }

    @PostMapping("/solutions")
    public ApiResponse<KnowledgeSolution> saveSolution(@RequestBody KnowledgeSolution solution) {
        return ApiResponse.success(knowledgeBaseService.saveSolution(solution));
    }

    @DeleteMapping("/solutions/{id}")
    public ApiResponse<Void> deleteSolution(@PathVariable Long id) {
        knowledgeBaseService.deleteSolution(id);
        return ApiResponse.success(null);
    }
}

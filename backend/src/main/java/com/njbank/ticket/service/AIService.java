package com.njbank.ticket.service;

import com.njbank.ticket.dto.AIAnalysisResponse;

public interface AIService {
    AIAnalysisResponse analyzeTitle(String title);
}

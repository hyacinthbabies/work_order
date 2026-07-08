package com.njbank.ticket.service;

import com.njbank.ticket.dto.DashboardDTO;

public interface DashboardService {
    DashboardDTO getDashboardData(String username, String role, String department);
}
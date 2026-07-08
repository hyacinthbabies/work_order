package com.njbank.ticket.controller;

import com.njbank.ticket.dto.ApiResponse;
import com.njbank.ticket.dto.DashboardDTO;
import com.njbank.ticket.entity.User;
import com.njbank.ticket.repository.UserRepository;
import com.njbank.ticket.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    
    private final DashboardService dashboardService;
    private final UserRepository userRepository;
    
    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }
    
    @GetMapping
    public ApiResponse<DashboardDTO> getDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("请先登录");
        }
        
        String username = authentication.getPrincipal().toString();
        User user = userRepository.findByUsername(username).orElse(null);
        
        String role = user != null ? user.getRole() : null;
        String department = user != null ? user.getDepartment() : null;
        
        DashboardDTO dashboard = dashboardService.getDashboardData(username, role, department);
        return ApiResponse.success(dashboard);
    }
}
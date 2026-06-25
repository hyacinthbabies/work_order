package com.njbank.ticket.controller;

import com.njbank.ticket.dto.ApiResponse;
import com.njbank.ticket.dto.LoginRequest;
import com.njbank.ticket.dto.LoginResponse;
import com.njbank.ticket.service.UserService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("OK");
    }
}
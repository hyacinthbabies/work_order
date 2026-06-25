package com.njbank.ticket.service;

import com.njbank.ticket.dto.LoginRequest;
import com.njbank.ticket.dto.LoginResponse;

public interface UserService {
    LoginResponse login(LoginRequest request);
}
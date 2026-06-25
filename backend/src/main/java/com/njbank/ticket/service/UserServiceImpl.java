package com.njbank.ticket.service;

import com.njbank.ticket.dto.LoginRequest;
import com.njbank.ticket.dto.LoginResponse;
import com.njbank.ticket.dto.UserDTO;
import com.njbank.ticket.entity.User;
import com.njbank.ticket.repository.UserRepository;
import com.njbank.ticket.security.JwtTokenUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        String token = jwtTokenUtil.generateToken(user.getUsername(), user.getRole());
        
        UserDTO userDTO = UserDTO.builder()
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .department(user.getDepartment())
                .build();
        
        return LoginResponse.builder()
                .token(token)
                .user(userDTO)
                .build();
    }
}
package com.microservices.auth.application.service;

import com.microservices.auth.application.dto.request.LoginRequest;
import com.microservices.auth.application.dto.request.RegisterRequest;
import com.microservices.auth.application.dto.response.AuthResponse;
import com.microservices.auth.application.dto.response.UserResponse;
import com.microservices.auth.application.mapper.UserMapper;
import com.microservices.auth.domain.exception.DomainException;
import com.microservices.auth.domain.model.User;
import com.microservices.auth.domain.service.UserDomainService;
import com.microservices.auth.infrastructure.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * APPLICATION SERVICE: AuthApplicationService
 * 
 * Orchestrates authentication use cases
 */
public class AuthApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthApplicationService.class);
    
    private final UserDomainService userDomainService;
    private final JwtTokenProvider jwtTokenProvider;
    
    // Constructor injection - now includes JwtTokenProvider
    public AuthApplicationService(UserDomainService userDomainService, 
                                   JwtTokenProvider jwtTokenProvider) {
        this.userDomainService = userDomainService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration request for email: {}", request.getEmail());
        
        try {
            User user = userDomainService.register(
                request.getEmail(),
                request.getUsername(),
                request.getPassword()
            );
            
            AuthResponse response = AuthResponse.forRegistration(
                user.getId().getValue().toString(),
                user.getUsername().getValue(),
                user.getEmail().getValue()
            );
            
            log.info("User registered successfully: {}", user.getId());
            return response;
            
        } catch (DomainException e) {
            log.error("Registration failed: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Login user - NOW GENERATES REAL JWT TOKEN
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login request for email: {}", request.getEmail());
        
        try {
            // Authenticate via domain service
            User user = userDomainService.login(
                request.getEmail(),
                request.getPassword()
            );
            
            // Generate REAL JWT token
            String token = jwtTokenProvider.generateToken(
                user.getId().getValue().toString(),
                user.getEmail().getValue()
            );
            
            // Build response with token
            AuthResponse response = AuthResponse.forLogin(
                token,
                user.getId().getValue().toString(),
                user.getUsername().getValue(),
                user.getEmail().getValue()
            );
            
            log.info("Login successful for user: {}", user.getId());
            return response;
            
        } catch (DomainException e) {
            log.error("Login failed: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get user by ID
     */
    public UserResponse getUserById(String userId) {
        log.info("Fetching user profile: {}", userId);
        
        try {
            User user = userDomainService.findById(userId);
            return UserMapper.toResponse(user);
            
        } catch (DomainException e) {
            log.error("Failed to fetch user: {}", e.getMessage());
            throw e;
        }
    }
}

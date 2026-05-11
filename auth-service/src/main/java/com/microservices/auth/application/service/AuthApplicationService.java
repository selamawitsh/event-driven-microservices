package com.microservices.auth.application.service;

import com.microservices.auth.application.dto.request.LoginRequest;
import com.microservices.auth.application.dto.request.RegisterRequest;
import com.microservices.auth.application.dto.response.AuthResponse;
import com.microservices.auth.application.dto.response.UserResponse;
import com.microservices.auth.application.mapper.UserMapper;
import com.microservices.auth.domain.exception.DomainException;
import com.microservices.auth.domain.model.User;
import com.microservices.auth.domain.service.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * APPLICATION SERVICE: AuthApplicationService
 * 
 * WHAT: Orchestrates authentication use cases
 * WHY: 
 * - Coordinates domain services
 * - Converts DTOs ↔ Domain objects
 * - Handles application-level concerns (logging)
 * - Provides clean API for presentation layer
 * 
 * USE CASES:
 * 1. User Registration
 * 2. User Login
 * 3. Get User Profile
 * 
 * WHY THIS IS NOT IN DOMAIN:
 * - Uses DTOs (application concern)
 * - Includes logging (infrastructure concern)
 * - Orchestrates multiple steps (workflow logic)
 */
public class AuthApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthApplicationService.class);
    
    private final UserDomainService userDomainService;
    
    // Constructor injection - dependencies are explicit
    public AuthApplicationService(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }
    
    /**
     * USE CASE: Register a new user
     * 
     * WORKFLOW:
     * 1. Extract data from DTO
     * 2. Call domain service (business logic)
     * 3. Map result to response DTO
     * 4. Return to presentation layer
     * 
     * WHY NOT IN CONTROLLER:
     * - Controller should be thin (just HTTP handling)
     * - Application logic should be testable without HTTP
     * - Can be reused by multiple controllers (REST, GraphQL, etc.)
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration request for email: {}", request.getEmail());
        
        try {
            // Delegate to domain service - it handles all business rules
            User user = userDomainService.register(
                request.getEmail(),
                request.getUsername(),
                request.getPassword()
            );
            
            // Convert domain object to response DTO
            AuthResponse response = AuthResponse.forRegistration(
                user.getId().getValue().toString(),
                user.getUsername().getValue(),
                user.getEmail().getValue()
            );
            
            log.info("User registered successfully: {}", user.getId());
            return response;
            
        } catch (DomainException e) {
            // Log and rethrow - let presentation layer handle HTTP status
            log.error("Registration failed: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * USE CASE: Login user
     * 
     * WORKFLOW:
     * 1. Validate credentials via domain service
     * 2. Generate JWT token (will be added)
     * 3. Return token + user info
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login request for email: {}", request.getEmail());
        
        try {
            // Authenticate via domain service
            User user = userDomainService.login(
                request.getEmail(),
                request.getPassword()
            );
            
            // TODO: Generate JWT token (will be added in infrastructure layer)
            String token = "JWT_TOKEN_PLACEHOLDER";
            
            // Build response
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
     * USE CASE: Get user profile by ID
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

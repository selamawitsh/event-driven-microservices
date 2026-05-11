package com.microservices.auth.infrastructure.config;

import com.microservices.auth.domain.event.EventPublisher;
import com.microservices.auth.domain.repository.UserRepository;
import com.microservices.auth.domain.service.UserDomainService;
import com.microservices.auth.application.service.AuthApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BEAN CONFIGURATION
 * 
 * WHAT: Creates Spring beans manually
 * WHY: 
 * - Domain services shouldn't have @Service (keeps them pure)
 * - Manual wiring makes dependencies explicit
 * - Easy to swap implementations for testing
 * 
 * WITH THIS CONFIG:
 * - Domain stays pure (no Spring annotations)
 * - Application services are wired explicitly
 * - Dependencies are clear and testable
 */
@Configuration
public class BeanConfig {
    
    /**
     * Create UserDomainService bean
     * 
     * Spring calls this method and manages the bean lifecycle
     * UserDomainService needs: UserRepository + EventPublisher
     */
    @Bean
    public UserDomainService userDomainService(
            UserRepository userRepository, 
            EventPublisher eventPublisher) {
        return new UserDomainService(userRepository, eventPublisher);
    }
    
    /**
     * Create AuthApplicationService bean
     * 
     * This is the main service used by controllers
     */
    @Bean
    public AuthApplicationService authApplicationService(
            UserDomainService userDomainService) {
        return new AuthApplicationService(userDomainService);
    }
}

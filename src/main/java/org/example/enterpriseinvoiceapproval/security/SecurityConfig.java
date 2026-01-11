package org.example.enterpriseinvoiceapproval.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Отключаем CSRF для тестов
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/invoices/**").permitAll() // Разрешаем доступ к инвойсам
                .anyRequest().permitAll() 
            );
        return http.build();
    }
}
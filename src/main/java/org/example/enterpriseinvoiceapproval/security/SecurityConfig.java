package org.example.enterpriseinvoiceapproval.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Настраиваем доступ к URL
                .authorizeHttpRequests(auth -> auth
                        // 1. Загружать инвойсы может любой авторизованный сотрудник
                        .requestMatchers(HttpMethod.POST, "/api/v1/invoices").authenticated()

                        // 2. Принимать решения (Approve/Reject) может ТОЛЬКО Менеджер
                        .requestMatchers(HttpMethod.PUT, "/api/v1/invoices/*/decision").hasRole("MANAGER")

                        // 3. Actuator и Swagger (если есть) оставляем открытыми для удобства
                        .requestMatchers("/actuator/**").permitAll()

                        // Все остальное закрыто
                        .anyRequest().authenticated()
                )
                // Подключаем OAuth2 Resource Server
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    // 👇 МАГИЯ: Конвертер, который учит Spring понимать роли Keycloak
    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    // Внутренний класс для извлечения ролей
    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            final Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

            if (realmAccess == null || realmAccess.isEmpty()) {
                return List.of();
            }

            // Достаем список ролей (например, ["MANAGER", "default-roles-ledgerflow"])
            List<String> roles = (List<String>) realmAccess.get("roles");

            // Превращаем их в Spring Security формат: ROLE_MANAGER
            return roles.stream()
                    .map(roleName -> "ROLE_" + roleName)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
    }
}
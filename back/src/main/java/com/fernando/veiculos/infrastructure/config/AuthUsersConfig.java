package com.fernando.veiculos.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class AuthUsersConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder,
                                                 @Value("${app.security.dev-users.admin.username:admin}") String adminUsername,
                                                 @Value("${app.security.dev-users.admin.password:admin123}") String adminPassword,
                                                 @Value("${app.security.dev-users.user.username:user}") String userUsername,
                                                 @Value("${app.security.dev-users.user.password:user123}") String userPassword) {
        return new InMemoryUserDetailsManager(
                User.withUsername(adminUsername)
                        .password(passwordEncoder.encode(adminPassword))
                        .roles("ADMIN")
                        .build(),
                User.withUsername(userUsername)
                        .password(passwordEncoder.encode(userPassword))
                        .roles("USER")
                        .build()
        );
    }
}

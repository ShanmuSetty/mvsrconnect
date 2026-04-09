package com.mvsr.mvsrconnect.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler successHandler;
    @Autowired
    private VendorAuthFilter vendorAuthFilter;

    public SecurityConfig(OAuth2LoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/login**","/actuator/health","/health","/events.html","/scanner.html","/sw.js", "/push/vapid-key","/icon-192.png", "/badge-72.png","/canteen/**","/canteen/vendor/login","/canteen/payment/webhook", "/canteen-vendor-login.html","/canteen-vendor.html","/scanner-canteen.html").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(vendorAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth -> oauth
                        .successHandler(successHandler)
                )
                .csrf(csrf -> csrf.disable())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                );

        return http.build();
    }
}

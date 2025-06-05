package com.auditing.audit.config; // Sesuaikan package Anda

import com.auditing.audit.service.UserDetailsServiceImpl; // Sesuaikan package service Anda
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // Ini benar

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ... konfigurasi HttpSecurity Anda ...
         http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorizeRequests ->
                    authorizeRequests
                            .requestMatchers("/login", "/register", "/css/**", "/js/**", "/error").permitAll()
                            .requestMatchers("/admin/**", "/users/add", "/users/save", "/users/list").hasAuthority("ROLE_ADMIN")
                            .requestMatchers("/kepalaspi/**").hasAuthority("ROLE_KEPALASPI")
                            .requestMatchers("/sekretaris/**").hasAuthority("ROLE_SEKRETARIS")
                            .requestMatchers("/karyawan/**").hasAuthority("ROLE_KARYAWAN")
                            .requestMatchers("/dashboard").hasAnyAuthority("ROLE_ADMIN", "ROLE_KEPALASPI", "ROLE_SEKRETARIS", "ROLE_KARYAWAN")
                            .anyRequest().authenticated()
            )
            // ... sisa konfigurasi ...
            .formLogin(formLogin ->
                    formLogin
                            .loginPage("/login")
                            .loginProcessingUrl("/perform_login")
                            .defaultSuccessUrl("/dashboard", true)
                            .failureUrl("/login?error=true")
                            .permitAll()
            )
            .logout(logout ->
                    logout
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/login?logout=true")
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            .permitAll()
            )
            .exceptionHandling(exceptionHandling ->
                    exceptionHandling.accessDeniedPage("/access-denied")
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { // Definisi bean PasswordEncoder
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Menggunakan UserDetailsServiceImpl yang di-inject
        authProvider.setPasswordEncoder(passwordEncoder()); // Menggunakan PasswordEncoder bean dari method di atas
        return authProvider;
    }

    // Tidak perlu @Bean UserDetailsService lagi jika sudah ada @Service UserDetailsServiceImpl
    // dan di-autowired di atas.
    // @Bean
    // public UserDetailsService userDetailsService() {
    // return userDetailsService;
    // }
}
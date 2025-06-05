package com.auditing.audit.config;

import com.auditing.audit.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Nonaktifkan CSRF untuk pengembangan (pertimbangkan keamanan untuk produksi)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/login", "/css/**", "/js/**", "/error").permitAll()
                                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN") // Akses /admin/** hanya untuk ROLE_ADMIN
                                .requestMatchers("/dashboard").hasAnyAuthority("ROLE_ADMIN", "ROLE_KEPALASPI", "ROLE_SEKRETARIS", "ROLE_KARYAWAN")
                                // Tambahkan path lain dan rolenya di sini
                                // .requestMatchers("/kepalaspi/**").hasAuthority("ROLE_KEPALASPI")
                                // .requestMatchers("/sekretaris/**").hasAuthority("ROLE_SEKRETARIS")
                                // .requestMatchers("/karyawan/**").hasAuthority("ROLE_KARYAWAN")
                                .anyRequest().authenticated() // Semua request lain butuh autentikasi
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login") // URL halaman login kustom
                                .loginProcessingUrl("/perform_login") // URL untuk submit form login (default Spring Security)
                                .defaultSuccessUrl("/dashboard", true) // URL setelah login sukses
                                .failureUrl("/login?error=true") // URL jika login gagal
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
                        exceptionHandling.accessDeniedPage("/access-denied") // Halaman kustom untuk akses ditolak
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
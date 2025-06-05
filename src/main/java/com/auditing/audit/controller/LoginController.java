package com.auditing.audit.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Username atau password salah!");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Anda berhasil logout.");
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Authentication authentication, Model model) {
        // Contoh mendapatkan role, bisa digunakan untuk logika tambahan di dashboard
        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            model.addAttribute("authorities", authorities);
        }
        return "dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDeniedPage() {
        return "access-denied";
    }
}
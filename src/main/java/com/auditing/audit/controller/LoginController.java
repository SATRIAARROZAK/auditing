package com.auditing.audit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

     // Halaman registrasi bisa ditambahkan di sini jika diperlukan
    // @GetMapping("/register")
    // public String registerPage(Model model) {
    // model.addAttribute("user", new com.example.auditingapp.model.User());
    // return "register";
    // }
}
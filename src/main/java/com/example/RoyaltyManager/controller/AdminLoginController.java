package com.example.RoyaltyManager.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminLoginController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "admin_login";
    }

    @PostMapping("/do-login")
    public String doLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {
        System.out.println("--- POST REACHED ---");
        System.out.println("Email: '" + email + "' Password: '" + password + "'");
        // Hardcoded admin credentials for step 1
        if ("admin@livestudio.com".equalsIgnoreCase(email.trim()) && "admin123".equals(password.trim())) {
            System.out.println("--- LOGIN SUCCESS ---");
            session.setAttribute("adminSession", "true");
            return "redirect:/dashboard";
        }
        System.out.println("--- LOGIN FAILED ---");
        return "redirect:/admin/login?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}

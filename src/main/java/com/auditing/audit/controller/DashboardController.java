package com.auditing.audit.controller;

import com.auditing.audit.model.AuditEntry;
import com.auditing.audit.model.User;
import com.auditing.audit.repository.UserRepository;
import com.auditing.audit.service.AuditEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid; // Jika menggunakan validasi

import java.util.List;

@Controller
@RequestMapping("/dashboard") // Semua endpoint di controller ini diawali /dashboard
public class DashboardController {

    @Autowired
    private AuditEntryService auditEntryService;

    @Autowired
    private UserRepository userRepository; // Untuk mendapatkan info user saat ini jika diperlukan

    // Menampilkan halaman dashboard utama dan daftar entri
    @GetMapping
    public String dashboardPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);
        model.addAttribute("currentUser", currentUser);

        List<AuditEntry> auditEntries;
        // Contoh: Admin dan KepalaSPI bisa lihat semua, yang lain hanya entri mereka
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Admin") || a.getAuthority().equals("ROLE_KepalaSPI"))) {
            auditEntries = auditEntryService.getAllAuditEntries();
        } else {
            auditEntries = auditEntryService.getAuditEntriesForCurrentUser();
        }

        model.addAttribute("auditEntries", auditEntries);
        model.addAttribute("newAuditEntry", new AuditEntry()); // Untuk form input
        return "dashboard"; // dashboard.html
    }

    // Menyimpan data input baru
    // Pastikan role yang sesuai diizinkan di SecurityConfig atau gunakan @PreAuthorize
    // @PreAuthorize("hasAnyRole('ADMIN', 'KEPALASPI', 'SEKRETARIS', 'KARYAWAN')")
    @PostMapping("/save")
    public String saveAuditEntry(@Valid @ModelAttribute("newAuditEntry") AuditEntry auditEntry,
                                 BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (result.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            // Jika ada error validasi, muat kembali data yang mungkin diperlukan untuk tampilan
            List<AuditEntry> auditEntries;
             if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Admin") || a.getAuthority().equals("ROLE_KepalaSPI"))) {
                auditEntries = auditEntryService.getAllAuditEntries();
            } else {
                auditEntries = auditEntryService.getAuditEntriesForCurrentUser();
            }
            model.addAttribute("auditEntries", auditEntries);
            return "dashboard"; // Kembali ke dashboard dengan error
        }

        auditEntryService.saveAuditEntry(auditEntry);
        redirectAttributes.addFlashAttribute("successMessage", "Data audit berhasil disimpan!");
        return "redirect:/dashboard";
    }


    // Contoh halaman khusus admin
    @GetMapping("/admin/settings")
    @PreAuthorize("hasRole('A')") // Hanya bisa diakses oleh ADMIN
    public String adminSettingsPage(Model model) {
        model.addAttribute("message", "Ini adalah halaman pengaturan khusus Admin.");
        return "admin_settings"; // Buat file admin_settings.html
    }

    // Endpoint untuk menghapus entri (contoh, bisa dikembangkan dengan konfirmasi)
    // @PreAuthorize("hasRole('ADMIN') or @customSecurityAuthorizer.canDeleteEntry(authentication, #entryId)") // contoh custom check
    @PostMapping("/delete/{id}")
    public String deleteAuditEntry(@PathVariable("id") Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        AuditEntry entry = auditEntryService.findById(id);
        if (entry == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Entri tidak ditemukan.");
            return "redirect:/dashboard";
        }

        User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);

        // Hanya Admin atau user yang membuat entri tersebut yang boleh menghapus
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Admin"));
        boolean isOwner = currentUser != null && entry.getCreatedBy() != null && entry.getCreatedBy().getId().equals(currentUser.getId());

        if (isAdmin || isOwner) {
            auditEntryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Entri berhasil dihapus.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Anda tidak memiliki izin untuk menghapus entri ini.");
        }
        return "redirect:/dashboard";
    }
}
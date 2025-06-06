// src/main/java/com/auditing/audit/controller/AuditController.java
package com.auditing.audit.controller;

import com.auditing.audit.model.AuditEntry;
import com.auditing.audit.model.User;
import com.auditing.audit.service.AuditEntryService;
import com.auditing.audit.service.UserService; // Jika diperlukan untuk info user
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("/audits") // Base path untuk semua yang terkait audit entries
public class AuditController {

    @Autowired
    private AuditEntryService auditEntryService;

    @Autowired
    private UserService userService; // Opsional, jika perlu info user detail

    // READ: Menampilkan semua audit entries (atau berdasarkan role)
    @GetMapping
    public String listAudits(Model model, Authentication authentication) {
        List<AuditEntry> auditEntries;
        boolean isAdminOrKepalaSpi = false;
        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            isAdminOrKepalaSpi = authorities.stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                                 a.getAuthority().equals("ROLE_KEPALASPI"));
        }

        if (isAdminOrKepalaSpi) {
            auditEntries = auditEntryService.getAllAuditEntries();
        } else {
            auditEntries = auditEntryService.getAuditEntriesForCurrentUser();
        }
        model.addAttribute("auditEntries", auditEntries);
        model.addAttribute("pageTitle", "Daftar Entri Audit");
        return "audits/list-audits"; // templates/audits/list-audits.html
    }

    // CREATE: Menampilkan form untuk membuat audit entry baru
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("auditEntry", new AuditEntry());
        model.addAttribute("pageTitle", "Tambah Entri Audit Baru");
        return "audits/form-audit"; // templates/audits/form-audit.html
    }

    // CREATE: Proses penyimpanan audit entry baru
    @PostMapping("/save")
    public String saveAudit(@Valid @ModelAttribute("auditEntry") AuditEntry auditEntry,
                            BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Tambah Entri Audit Baru");
            return "audits/form-audit";
        }
        try {
            auditEntryService.saveAuditEntry(auditEntry);
            redirectAttributes.addFlashAttribute("successMessage", "Entri audit berhasil disimpan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menyimpan entri audit: " + e.getMessage());
        }
        return "redirect:/audits";
    }

    // UPDATE: Menampilkan form untuk mengedit audit entry
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return auditEntryService.findById(id)
                .map(auditEntry -> {
                    model.addAttribute("auditEntry", auditEntry);
                    model.addAttribute("pageTitle", "Edit Entri Audit");
                    return "audits/form-audit"; // Menggunakan form yang sama untuk create dan update
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Entri audit dengan ID " + id + " tidak ditemukan.");
                    return "redirect:/audits";
                });
    }

    // UPDATE: Proses update audit entry
    @PostMapping("/update/{id}")
    public String updateAudit(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("auditEntry") AuditEntry auditEntry,
                              BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Entri Audit");
            // auditEntry.setId(id); // Pastikan ID tetap ada di model jika validasi gagal
            return "audits/form-audit";
        }
        try {
            auditEntryService.updateAuditEntry(id, auditEntry);
            redirectAttributes.addFlashAttribute("successMessage", "Entri audit berhasil diperbarui!");
        } catch (ResourceNotFoundException e) {
             redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui entri audit: " + e.getMessage());
        }
        return "redirect:/audits";
    }

    // DELETE: Proses penghapusan audit entry
    @PostMapping("/delete/{id}")
    public String deleteAudit(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            auditEntryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Entri audit berhasil dihapus!");
        } catch (ResourceNotFoundException e) {
             redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus entri audit: " + e.getMessage());
        }
        return "redirect:/audits";
    }
}
package com.auditing.audit.service;

import com.auditing.audit.model.AuditEntry;
import com.auditing.audit.model.User;
import com.auditing.audit.repository.AuditEntryRepository;
import com.auditing.audit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditEntryService {

    private final AuditEntryRepository auditEntryRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuditEntryService(AuditEntryRepository auditEntryRepository, UserRepository userRepository) {
        this.auditEntryRepository = auditEntryRepository;
        this.userRepository = userRepository;
    }

    public AuditEntry saveAuditEntry(AuditEntry auditEntry) {
        // Dapatkan user yang sedang login
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        User currentUser = userRepository.findByUsername(currentPrincipalName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentPrincipalName));

        auditEntry.setCreatedBy(currentUser);
        return auditEntryRepository.save(auditEntry);
    }

    public List<AuditEntry> getAllAuditEntries() {
        return auditEntryRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<AuditEntry> getAuditEntriesForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        User currentUser = userRepository.findByUsername(currentPrincipalName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentPrincipalName));
        return auditEntryRepository.findByCreatedBy(currentUser);
    }

    public AuditEntry findById(Long id) {
        return auditEntryRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        auditEntryRepository.deleteById(id);
    }
}
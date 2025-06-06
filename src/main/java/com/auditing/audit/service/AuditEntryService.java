// // src/main/java/com/auditing/audit/service/AuditEntryService.java
// package com.auditing.audit.service;

// import com.auditing.audit.model.AuditEntry;
// import com.auditing.audit.model.User;
// import com.auditing.audit.repository.AuditEntryRepository;
// import com.auditing.audit.repository.UserRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional; // Import Transactional

// import java.time.LocalDateTime; // Import LocalDateTime
// import java.util.List;
// import java.util.Optional; // Import Optional

// @Service
// public class AuditEntryService {

//     private final AuditEntryRepository auditEntryRepository;
//     private final UserRepository userRepository;

//     @Autowired
//     public AuditEntryService(AuditEntryRepository auditEntryRepository, UserRepository userRepository) {
//         this.auditEntryRepository = auditEntryRepository;
//         this.userRepository = userRepository;
//     }

//     private User getCurrentAuthenticatedUser() {
//         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//         if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
//             // Bisa throw exception atau return null tergantung kebijakan aplikasi jika tidak ada user login
//             // Untuk CRUD, biasanya user harus login
//             throw new IllegalStateException("User tidak terautentikasi untuk melakukan operasi ini.");
//         }
//         String currentPrincipalName = authentication.getName();
//         return userRepository.findByUsername(currentPrincipalName)
//                 .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + currentPrincipalName));
//     }

//     @Transactional
//     public AuditEntry saveAuditEntry(AuditEntry auditEntry) {
//         User currentUser = getCurrentAuthenticatedUser();
//         auditEntry.setCreatedBy(currentUser);
//         // createdAt dan updatedAt akan di-handle oleh @PrePersist
//         return auditEntryRepository.save(auditEntry);
//     }

//     @Transactional
//     public AuditEntry updateAuditEntry(Long id, AuditEntry auditEntryDetails) {
//         AuditEntry existingAuditEntry = auditEntryRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("AuditEntry tidak ditemukan dengan id " + id));

//         User currentUser = getCurrentAuthenticatedUser();
//         // Optional: Tambahkan validasi apakah user yang mengedit adalah pembuatnya atau admin
//         // if (!existingAuditEntry.getCreatedBy().equals(currentUser) && !currentUser.getRole().equals("ROLE_ADMIN")) {
//         //     throw new AccessDeniedException("Anda tidak memiliki izin untuk mengedit entri ini.");
//         // }


//         existingAuditEntry.setTitle(auditEntryDetails.getTitle());
//         existingAuditEntry.setDescription(auditEntryDetails.getDescription());
//         existingAuditEntry.setCategory(auditEntryDetails.getCategory());
//         // createdBy tidak diubah saat update
//         // updatedAt akan di-handle oleh @PreUpdate
//         return auditEntryRepository.save(existingAuditEntry);
//     }


//     public List<AuditEntry> getAllAuditEntries() {
//         return auditEntryRepository.findAllByOrderByCreatedAtDesc();
//     }

//     public List<AuditEntry> getAuditEntriesForCurrentUser() {
//         User currentUser = getCurrentAuthenticatedUser();
//         return auditEntryRepository.findByCreatedByOrderByCreatedAtDesc(currentUser);
//     }

//     public Optional<AuditEntry> findById(Long id) { // Mengembalikan Optional
//         return auditEntryRepository.findById(id);
//     }

//     @Transactional
//     public void deleteById(Long id) {
//         AuditEntry existingAuditEntry = auditEntryRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("AuditEntry tidak ditemukan dengan id " + id));

//         User currentUser = getCurrentAuthenticatedUser();
//         // Optional: Tambahkan validasi apakah user yang menghapus adalah pembuatnya atau admin
//         // if (!existingAuditEntry.getCreatedBy().equals(currentUser) && !currentUser.getRole().equals("ROLE_ADMIN")) {
//         //    throw new AccessDeniedException("Anda tidak memiliki izin untuk menghapus entri ini.");
//         // }

//         auditEntryRepository.deleteById(id);
//     }
// }

// // Buat custom exception jika belum ada
// class ResourceNotFoundException extends RuntimeException {
//     public ResourceNotFoundException(String message) {
//         super(message);
//     }
// }
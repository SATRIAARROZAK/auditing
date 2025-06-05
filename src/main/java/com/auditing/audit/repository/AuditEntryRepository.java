package com.auditing.audit.repository;

import com.auditing.audit.model.AuditEntry;
import com.auditing.audit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {
    List<AuditEntry> findByCreatedBy(User user);
    List<AuditEntry> findAllByOrderByCreatedAtDesc(); // Menampilkan data terbaru dulu
}
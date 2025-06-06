package com.auditing.audit;

import com.auditing.audit.model.User;
import com.auditing.audit.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuditApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty() && userRepository.findByEmail("admin@example.com").isEmpty()) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@example.com");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setRole("ROLE_ADMIN"); // Simpan dengan prefix ROLE_
                adminUser.setEnabled(true);
                userRepository.save(adminUser);
                System.out.println("Admin user created: admin / admin123 (ROLE_ADMIN)");
            }

            if (userRepository.findByUsername("kepalaspi").isEmpty() && userRepository.findByEmail("kepalaspi@example.com").isEmpty()) {
                User kepalaSpiUser = new User();
                kepalaSpiUser.setUsername("kepalaspi");
                kepalaSpiUser.setEmail("kepalaspi@example.com");
                kepalaSpiUser.setPassword(passwordEncoder.encode("kepala123"));
                kepalaSpiUser.setRole("ROLE_KEPALASPI");
                kepalaSpiUser.setEnabled(true);
                userRepository.save(kepalaSpiUser);
                System.out.println("Kepala SPI user created: kepalaspi / kepala123 (ROLE_KEPALASPI)");
            }

            if (userRepository.findByUsername("karyawan").isEmpty() && userRepository.findByEmail("karyawan@example.com").isEmpty()) {
                User karyawanUser = new User();
                karyawanUser.setUsername("karyawan");
                karyawanUser.setEmail("karyawan@example.com");
                karyawanUser.setPassword(passwordEncoder.encode("karyawan123"));
                karyawanUser.setRole("ROLE_KARYAWAN");
                karyawanUser.setEnabled(true);
                userRepository.save(karyawanUser);
                System.out.println("Karyawan user created: karyawan / karyawan123 (ROLE_KARYAWAN)");
            }
        };
    }
}
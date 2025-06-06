package com.auditing.audit.service;

import com.auditing.audit.dto.UserDto;
import com.auditing.audit.model.User;
import com.auditing.audit.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException; // Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // Import

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User saveUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        String role = userDto.getRole();
        if (role != null && !role.toUpperCase().startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }
        user.setRole(role);
        user.setEnabled(true); // User baru defaultnya enabled
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUser(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User dengan ID " + id + " tidak ditemukan"));

        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());

        // Hanya update password jika diisi di DTO
        if (StringUtils.hasText(userDto.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        String role = userDto.getRole();
        if (role != null && !role.toUpperCase().startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }
        existingUser.setRole(role);
        // Anda mungkin ingin menambahkan field 'enabled' ke UserDto jika ingin bisa diubah
        // Untuk saat ini, kita tidak mengubah status enabled saat update biasa
        // existingUser.setEnabled(userDto.isEnabled());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User dengan ID " + id + " tidak ditemukan, tidak dapat dihapus.");
        }
        // Tambahan: Logika untuk mencegah admin menghapus dirinya sendiri jika diperlukan
        // Misalnya, dapatkan user yang sedang login dan bandingkan ID nya.
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // String currentUsername = authentication.getName();
        // User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        // if (currentUser != null && currentUser.getId().equals(id)) {
        // throw new IllegalArgumentException("Admin tidak dapat menghapus akunnya sendiri.");
        // }

        userRepository.deleteById(id);
    }
}
package com.auditing.audit.service;

import com.auditing.audit.model.User;
import com.auditing.audit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // TIDAK ADA @Autowired PasswordEncoder di sini

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);

        User user = userOptional.orElseThrow(() ->
                new UsernameNotFoundException("User tidak ditemukan dengan username atau email: " + usernameOrEmail));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getAuthorities(user.getRole())
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String roleName) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (roleName != null && !roleName.trim().isEmpty()) {
            // Pastikan role memiliki prefix ROLE_ jika Spring Security memerlukannya
            // dan role dari DB belum memiliki prefix
            if (!roleName.toUpperCase().startsWith("ROLE_")) {
                 authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));
            } else {
                 authorities.add(new SimpleGrantedAuthority(roleName.toUpperCase()));
            }
        }
        return authorities;
    }
}
package com.auditing.audit.service;

import com.auditing.audit.dto.UserDto;
import com.auditing.audit.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(UserDto userDto);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllUsers();
}
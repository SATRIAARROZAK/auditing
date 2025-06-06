package com.auditing.audit.dto;

import com.auditing.audit.validation.OnCreate;
import com.auditing.audit.validation.OnUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size; // Kita akan gunakan ini hanya untuk OnCreate
import lombok.Data;

@Data
public class UserDto {
    @NotEmpty(message = "Username tidak boleh kosong", groups = {OnCreate.class, OnUpdate.class})
    private String username;

    @NotEmpty(message = "Email tidak boleh kosong", groups = {OnCreate.class, OnUpdate.class})
    @Email(message = "Format email tidak valid", groups = {OnCreate.class, OnUpdate.class})
    private String email;

    @NotEmpty(message = "Password tidak boleh kosong", groups = OnCreate.class)
    @Size(min = 6, message = "Password minimal 6 karakter", groups = OnCreate.class) // Hanya berlaku saat Create
    private String password; // Untuk Update, validasi panjang akan dilakukan di Controller jika diisi

    @NotEmpty(message = "Role tidak boleh kosong", groups = {OnCreate.class, OnUpdate.class})
    private String role;
}
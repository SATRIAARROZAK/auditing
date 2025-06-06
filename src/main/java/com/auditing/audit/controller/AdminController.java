package com.auditing.audit.controller;

import com.auditing.audit.dto.UserDto; //
import com.auditing.audit.model.User; //
import com.auditing.audit.service.UserService; //
import jakarta.validation.Valid; //
import org.springframework.beans.factory.annotation.Autowired; //
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller; //
import org.springframework.ui.Model; //
import org.springframework.validation.BindingResult; //
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.Arrays; //
import java.util.List; //
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService; //

    private final List<String> availableRoles = Arrays.asList("ADMIN", "KEPALASPI", "SEKRETARIS", "KARYAWAN"); //

    @GetMapping("/users/add")
    public String showAddUserForm(Model model) { //
        model.addAttribute("userDto", new UserDto()); //
        model.addAttribute("formTitle", "Tambah User Baru");
        model.addAttribute("availableRoles", availableRoles); //
        return "admin/user-form"; // Menggunakan satu form untuk add dan edit
    }

    @PostMapping("/users/save")
    public String saveUser(@Valid @ModelAttribute("userDto") UserDto userDto, //
                           BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        Optional<User> existingUserByUsername = userService.findByUsername(userDto.getUsername()); //
        if (existingUserByUsername.isPresent()) { //
            result.rejectValue("username", "username.exists", "Username sudah digunakan"); //
        }
        Optional<User> existingUserByEmail = userService.findByEmail(userDto.getEmail()); //
        if (existingUserByEmail.isPresent()) { //
            result.rejectValue("email", "email.exists", "Email sudah digunakan"); //
        }

        if (result.hasErrors()) { //
            model.addAttribute("availableRoles", availableRoles); //
             model.addAttribute("formTitle", "Tambah User Baru");
            return "admin/user-form"; //
        }

        try {
            userService.saveUser(userDto); //
            redirectAttributes.addFlashAttribute("successMessage", "User baru berhasil ditambahkan!");
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("availableRoles", availableRoles);
             model.addAttribute("formTitle", "Tambah User Baru");
            model.addAttribute("errorMessage", "Gagal menyimpan user. Username atau Email mungkin sudah ada.");
            return "admin/user-form";
        }
        return "redirect:/admin/users/list"; //
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User tidak ditemukan!");
            return "redirect:/admin/users/list";
        }
        User user = userOptional.get();
        UserDto userDto = new UserDto(); //
        userDto.setUsername(user.getUsername()); //
        userDto.setEmail(user.getEmail()); //
        // Untuk role, kita ambil bagian setelah "ROLE_" jika ada, agar sesuai dengan value di dropdown
        String currentRole = user.getRole(); //
        if (currentRole != null && currentRole.toUpperCase().startsWith("ROLE_")) {
            userDto.setRole(currentRole.substring(5));
        } else {
            userDto.setRole(currentRole);
        }
        // Password tidak di-set di DTO untuk edit, biarkan kosong jika tidak ingin diubah

        model.addAttribute("userDto", userDto);
        model.addAttribute("userId", id);
        model.addAttribute("formTitle", "Edit User: " + user.getUsername());
        model.addAttribute("availableRoles", availableRoles);
        return "admin/user-form"; // Menggunakan form yang sama
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("userDto") UserDto userDto,
                             BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        // Validasi untuk username dan email hanya jika diubah dan sudah ada pada user lain
        Optional<User> existingUser = userService.findById(id);
        if (existingUser.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "User tidak ditemukan!");
            return "redirect:/admin/users/list";
        }

        User currentUser = existingUser.get();

        if (!currentUser.getUsername().equals(userDto.getUsername())) {
            userService.findByUsername(userDto.getUsername()).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    result.rejectValue("username", "username.exists", "Username sudah digunakan.");
                }
            });
        }
        if (!currentUser.getEmail().equals(userDto.getEmail())) {
            userService.findByEmail(userDto.getEmail()).ifPresent(u -> {
                 if (!u.getId().equals(id)) {
                    result.rejectValue("email", "email.exists", "Email sudah digunakan.");
                }
            });
        }


        // Password di UserDto hanya diisi jika ingin diubah, jadi validasi @NotEmpty mungkin tidak relevan
        // Jika password diisi tapi kurang dari 6 karakter
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty() && userDto.getPassword().length() < 6) {
            result.rejectValue("password", "password.length", "Password minimal 6 karakter jika diubah.");
        }


        if (result.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("formTitle", "Edit User: " + currentUser.getUsername());
            model.addAttribute("availableRoles", availableRoles);
            return "admin/user-form";
        }

        try {
            userService.updateUser(id, userDto);
            redirectAttributes.addFlashAttribute("successMessage", "User berhasil diperbarui!");
        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            model.addAttribute("userId", id);
            model.addAttribute("formTitle", "Edit User: " + currentUser.getUsername());
            model.addAttribute("availableRoles", availableRoles);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/user-form";
        }
        return "redirect:/admin/users/list";
    }


    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
             userService.deleteUserById(id);
            redirectAttributes.addFlashAttribute("successMessage", "User berhasil dihapus!");
        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            // Jika user memiliki data terkait yang tidak bisa dihapus karena constraint DB
             redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus user. User mungkin memiliki data terkait.");
        }
        return "redirect:/admin/users/list";
    }


    @GetMapping("/users/list")
    public String listUsers(Model model) { //
        List<User> users = userService.findAllUsers(); //
        model.addAttribute("users", users); //
        return "admin/list-user"; //
    }
}
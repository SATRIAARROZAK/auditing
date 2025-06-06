package com.auditing.audit.controller;

import com.auditing.audit.dto.UserDto; 
import com.auditing.audit.model.User; 
import com.auditing.audit.service.UserService; 
import com.auditing.audit.validation.OnCreate;
import com.auditing.audit.validation.OnUpdate;

import jakarta.validation.Valid; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model; 
import org.springframework.validation.BindingResult; 
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays; 
import java.util.List; 
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService; 

    private final List<String> availableRoles = Arrays.asList("ADMIN", "KEPALASPI", "SEKRETARIS", "KARYAWAN"); 

    @GetMapping("/users/add")
    public String showAddUserForm(Model model) { 
        model.addAttribute("userDto", new UserDto()); 
        model.addAttribute("formTitle", "Tambah User Baru");
        model.addAttribute("availableRoles", availableRoles); 
        return "admin/user-form"; 
    }

    @PostMapping("/users/save")
    public String saveUser(@Validated(OnCreate.class) @ModelAttribute("userDto") UserDto userDto, 
                           BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        //  Validasi unik username dan email
        if (!result.hasFieldErrors("username") && userService.findByUsername(userDto.getUsername()).isPresent()) { 
            result.rejectValue("username", "username.exists", "Username sudah digunakan"); 
        }
        if (!result.hasFieldErrors("email") && userService.findByEmail(userDto.getEmail()).isPresent()) { 
            result.rejectValue("email", "email.exists", "Email sudah digunakan"); 
        }

        if (result.hasErrors()) { 
            model.addAttribute("availableRoles", availableRoles); 
            model.addAttribute("formTitle", "Tambah User Baru");
            return "admin/user-form";
        }

        try {
            userService.saveUser(userDto); 
            redirectAttributes.addFlashAttribute("successMessage", "User baru berhasil ditambahkan!");
        } catch (DataIntegrityViolationException e) {
            //  Ini akan menangkap error jika saveUser gagal karena constraint unik di DB
            //  meskipun kita sudah cek di atas, ini sebagai lapisan pengaman tambahan
            model.addAttribute("availableRoles", availableRoles);
            model.addAttribute("formTitle", "Tambah User Baru");
            model.addAttribute("errorMessage", "Gagal menyimpan user. Username atau Email mungkin sudah ada.");
            return "admin/user-form";
        }
        return "redirect:/admin/users/list"; 
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User tidak ditemukan!");
            return "redirect:/admin/users/list";
        }
        User user = userOptional.get();
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        String currentRole = user.getRole();
        if (currentRole != null && currentRole.toUpperCase().startsWith("ROLE_")) {
            userDto.setRole(currentRole.substring(5));
        } else {
            userDto.setRole(currentRole);
        }
        //  Password tidak di-set di DTO, akan null

        model.addAttribute("userDto", userDto);
        model.addAttribute("userId", id);
        model.addAttribute("formTitle", "Edit User: " + user.getUsername());
        model.addAttribute("availableRoles", availableRoles);
        return "admin/user-form";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @Validated(OnUpdate.class) @ModelAttribute("userDto") UserDto userDto,
                             BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        Optional<User> existingUserOpt = userService.findById(id);
        if (existingUserOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "User tidak ditemukan!");
            return "redirect:/admin/users/list";
        }
        User currentUser = existingUserOpt.get();

        //  Validasi unik username (jika diubah)
        if (!result.hasFieldErrors("username") && !currentUser.getUsername().equals(userDto.getUsername())) {
            userService.findByUsername(userDto.getUsername()).ifPresent(u -> {
                if (!u.getId().equals(id)) { 
                    result.rejectValue("username", "username.exists", "Username sudah digunakan.");
                }
            });
        }
        //  Validasi unik email (jika diubah)
        if (!result.hasFieldErrors("email") && !currentUser.getEmail().equals(userDto.getEmail())) {
            userService.findByEmail(userDto.getEmail()).ifPresent(u -> {
                 if (!u.getId().equals(id)) { 
                    result.rejectValue("email", "email.exists", "Email sudah digunakan.");
                }
            });
        }

        //  Validasi panjang password HANYA JIKA password diisi
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            if (userDto.getPassword().length() < 6) {
                result.rejectValue("password", "Size.userDto.password", "Password minimal 6 karakter jika diubah.");
            }
        } else {
            //  Jika password kosong (artinya tidak ingin diubah), kita perlu menghapus error
            //  yang mungkin muncul dari validasi @Size di DTO jika itu masih ada dan berlaku.
            //  Dengan pendekatan DTO yang direvisi (menghapus @Size dari password untuk OnUpdate),
            //  baris ini mungkin tidak diperlukan, tetapi aman untuk berjaga-jaga.
            if (result.hasFieldErrors("password")) {
                //  Cek apakah errornya karena @NotEmpty (tidak relevan untuk update password opsional)
                //  atau @Size (relevan hanya jika diisi)
                //  Ini sedikit rumit, lebih baik @Size tidak diterapkan pada password di DTO untuk grup OnUpdate
            }
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
            //  Jika user memiliki data terkait yang tidak bisa dihapus karena constraint DB
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Gagal menghapus user. User mungkin memiliki data terkait.");
        }
        return "redirect:/admin/users/list";
    }

    @GetMapping("/users/list")
    public String listUsers(Model model) { 
        List<User> users = userService.findAllUsers(); 
        model.addAttribute("users", users); 
        return "admin/list-user"; 
    }
}
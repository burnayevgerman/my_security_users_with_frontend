package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.RoleService;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final RoleService roleService;

    public AdminController(UserService userService,
                           RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String desktopPage(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());

        model.addAttribute("user", user);

        model.addAttribute("roles", user.getRoles().stream()
                .map(Role::getName).collect(Collectors.toSet()));

        model.addAttribute("page", "PAGE_ADMIN");

        model.addAttribute("users", userService.getAllUsers());

        model.addAttribute("usersViewRoles",
                userService.getAllUsers()
                        .stream()
                        .collect(Collectors.toMap(
                                User::getId,
                                u -> u.getRoles()
                                        .stream()
                                        .map(Role::getViewText)
                                        .collect(Collectors.joining(", "))))
        );

        model.addAttribute("newUser", new User());

        return "desktop";
    }

    @PostMapping("/create")
    public String createUser(@RequestParam(value = "selectedRole") String selectedRole,
                             @ModelAttribute(name = "user") User user) {
        user.setRoles(new HashSet<>(Set.of(roleService.findRoleByName("ROLE_USER"))));

        if (selectedRole.equals("ROLE_ADMIN")) {
            user.getRoles().add(roleService.findRoleByName("ROLE_ADMIN"));
        }

        user = userService.createUser(user);

        if (user == null) {
            System.out.println("Failed to create a new user!");
            System.out.println(user);
            return "redirect:/admin?create_error";
        }

        return String.format("redirect:/admin?create_success&id=%d", user.getId());
    }

    @PutMapping("/edit")
    public String editUser(@RequestParam(value = "selectedRole") String selectedRole,
                           @ModelAttribute("user") User user) {
        try {
            user.setRoles(new HashSet<>(Set.of(roleService.findRoleByName("ROLE_USER"))));

            if (selectedRole.equals("ROLE_ADMIN")) {
                user.getRoles().add(roleService.findRoleByName("ROLE_ADMIN"));
            }

            if (userService.updateUser(user) == null) {
                System.out.println("The changes could not be saved to the database!");
                System.out.println(user);
                return String.format("redirect:/admin?update_error&id=%d", user.getId());
            }
            return String.format("redirect:/admin?update_success&id=%d", user.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return String.format("redirect:/admin?update_error&id=%d", user.getId());
        }
    }

    @DeleteMapping("/delete")
    public String deleteUser(@RequestParam("id") long id) {
        if (!userService.deleteUserById(id)) {
            System.out.println("Couldn't delete user with id: " + id);
            return String.format("redirect:/admin?delete_error&id=%d", id);
        }

        return String.format("redirect:/admin?delete_success&id=%d", id);
    }
}

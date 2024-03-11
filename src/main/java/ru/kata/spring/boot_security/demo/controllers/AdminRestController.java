package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.UserService;

@RestController
@RequestMapping("/admin")
public class AdminRestController {
    private final UserService userService;

    public AdminRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user-info/{id}")
    public User getUserById(@PathVariable("id") long id) {
        User user = userService.getUserById(id);
        user.setPassword(null);
        return user;
    }

    @GetMapping("/user-role/{id}")
    public String getRoleById(@PathVariable("id") long id) {
        if (userService.getUserById(id).getRoles()
                .stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
            return "ROLE_ADMIN";
        } else {
            return "ROLE_USER";
        }
    }
}

package ru.kata.spring.boot_security.demo.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final RoleServiceImpl roleService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RoleServiceImpl roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException(
                    String.format("User with username '%s' not found!", username)
            );
        }

        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email).orElse(null);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if ((getUserByEmail(user.getEmail()) != null) || user.getPassword().isEmpty()) {
            return null;
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        roleService.getAllRoles().stream()
                .filter(Role::isRequired)
                .forEach(r -> user.getRoles().add(r));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        if (user.getPassword().isEmpty()) {
            user.setPassword(userRepository.getById(user.getId()).getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        roleService.getAllRoles().stream()
                .filter(Role::isRequired)
                .forEach(r -> user.getRoles().add(r));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean deleteUserById(long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }

        return false;
    }
}

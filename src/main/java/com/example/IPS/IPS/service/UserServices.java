package com.example.IPS.IPS.service;


import com.example.IPS.IPS.entity.Users;
import com.example.IPS.IPS.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserServices {
    private final PasswordEncoder passwordEncoder; // Inject the BCryptPasswordEncoder bean

    private final UsersRepository usersRepository;

    UserServices(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //    user register
    public Users registerUsers(Users users) {
        String encodedPassword = passwordEncoder.encode(users.getPassword());
        Users user = new Users();
        user.setUsername(users.getUsername());
        user.setPassword(encodedPassword);
        user.setRole(users.getRole());
        return usersRepository.save(user);
    }

    //    get all users
    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    //    get user detail
    public Users getUserByUsername(String username) {
        return usersRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));


    }

}

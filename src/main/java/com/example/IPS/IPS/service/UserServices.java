package com.example.IPS.IPS.service;


import com.example.IPS.IPS.entity.Users;
import com.example.IPS.IPS.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserServices {
private UsersRepository usersRepository;
    UserServices(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }
//    user register
    public Users registerUsers(Users users) {
        usersRepository.save(users);
        return users;
    }
//    get all users
    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }
//    get user detail
    public Users getUserByUsername(String username) {
        return usersRepository.findByUsername(username);
    }

}

package com.example.IPS.IPS.controller;

import com.example.IPS.IPS.entity.Users;
import com.example.IPS.IPS.service.UserServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {
  private   UserServices userServices;
    UsersController(UserServices userServices) {
        this.userServices = userServices;
    }
//    get all
    @GetMapping("")
    public ResponseEntity<List<Users>> getAllUsers(){

        return ResponseEntity.ok(userServices.getAllUsers());
    }
//    get by username
    @GetMapping("/{username}")
    public ResponseEntity<Users> getUsersByUsername(@PathVariable String username){
        return ResponseEntity.ok(userServices.getUserByUsername(username));
    }
//    register
    @PostMapping("/register")
    public ResponseEntity<Users> registerNewUser(@RequestBody Users user){
        return ResponseEntity.ok(userServices.registerUsers(user));
    }
}

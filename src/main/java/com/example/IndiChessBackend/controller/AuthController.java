package com.example.IndiChessBackend.controller;


import com.example.IndiChessBackend.model.DTO.LoginDto;
import com.example.IndiChessBackend.model.User;
import com.example.IndiChessBackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    // we want users to get stored in db
    // then use those credentials to login

    private final AuthService authservice;


    @PostMapping("/signup")
    public ResponseEntity<User> handleSignup(@RequestBody User user){
        return new ResponseEntity<>(authservice.save(user), HttpStatus.CREATED);
    }



}
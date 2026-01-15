package com.example.IndiChessBackend.service;

import com.example.IndiChessBackend.model.User;
import com.example.IndiChessBackend.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public String getUserbyUsername(String username) {
        return userRepo.getUserByUsername(username).getUsername();
    }
    public User findByEmail(String email){
        return userRepo.getUserByEmailId(email);
    }


}

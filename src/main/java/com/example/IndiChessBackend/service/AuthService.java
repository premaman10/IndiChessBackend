package com.example.IndiChessBackend.service;

import com.example.IndiChessBackend.model.User;
import com.example.IndiChessBackend.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;

    public User save(User user){
        user.setRating(250);//default rating

        return userRepo.save(user);
    }

}
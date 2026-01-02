package com.example.IndiChessBackend.service;


import com.example.IndiChessBackend.repo.UserRepo;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

@Service
public class UserDetailService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // You can modify this method to support email-based authentication as well
        com.example.IndiChessBackend.model.User user = userRepo.findByUserName(username);
//        System.out.println(user);
//        System.out.println(user.getPassword());
        if(user == null) throw new UsernameNotFoundException("User not found with username: " + username);

        return new org.springframework.security.core.userdetails.User(
                user.getUserName(),
                user.getPassword(),
                new ArrayList<>()); // You can add authorities (roles) here as needed
    }
}
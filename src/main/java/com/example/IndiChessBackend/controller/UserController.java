package com.example.IndiChessBackend.controller;

import com.example.IndiChessBackend.model.DTO.LoginDto;
import com.example.IndiChessBackend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @GetMapping("/hello")
    public String show(){
        return "Hello";
    }

    @GetMapping("/world")
    public String show2(){
        return "World";
    }

    @PostMapping("/login")
    public ResponseEntity<String> handleLogin(HttpServletRequest request,
                                              HttpServletResponse response,
                                              @RequestBody LoginDto loginDto){
        request.getSession(false);
        Authentication authObject = authenticationManager.
                authenticate(new
                        UsernamePasswordAuthenticationToken
                        (loginDto.getUsername(), loginDto.getPassword()));
        if(authObject.isAuthenticated()) {
//            System.out.println("Token ");
            return new ResponseEntity<>(jwtService.generateToken(loginDto.getUsername()), HttpStatus.OK);
        }

        return new ResponseEntity<>("Auth Failed", HttpStatus.BAD_REQUEST);
    }

}
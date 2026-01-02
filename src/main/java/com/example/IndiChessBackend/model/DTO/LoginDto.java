package com.example.IndiChessBackend.model.DTO;


import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class LoginDto {
    String username;
    String password;
}
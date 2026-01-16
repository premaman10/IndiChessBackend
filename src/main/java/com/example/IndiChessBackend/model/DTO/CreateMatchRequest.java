package com.example.IndiChessBackend.model.DTO;

import com.example.IndiChessBackend.model.GameType;
import lombok.Data;

@Data
public class CreateMatchRequest {
    private GameType gameType = GameType.STANDARD; // Default to STANDARD if not provided
}

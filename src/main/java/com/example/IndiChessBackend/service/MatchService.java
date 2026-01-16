package com.example.IndiChessBackend.service;


import com.example.IndiChessBackend.model.DTO.CreateMatchRequest;
import com.example.IndiChessBackend.model.GameType;
import com.example.IndiChessBackend.model.Match;
import com.example.IndiChessBackend.model.User;
import com.example.IndiChessBackend.repo.MatchRepo;
import com.example.IndiChessBackend.repo.UserRepo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.IndiChessBackend.model.MatchStatus.IN_PROGRESS;

@Service
public class MatchService {

    // Store waiting players: key = username, value = gameType (as string for matching)
    private static final Map<String, GameType> waitingPlayers = new ConcurrentHashMap<>();
    private static final Map<Long, String[]> matchPlayers = new ConcurrentHashMap<>();

    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final MatchRepo matchRepo;
    private final GameService gameService;

    @Autowired
    MatchService(JwtService jwtService, UserRepo userRepo, MatchRepo matchRepo, GameService gameService) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.matchRepo = matchRepo;
        this.gameService = gameService;

        // Clean up old entries periodically (optional)
        new Timer().schedule(new TimerTask() {
            public void run() {
                cleanupOldEntries();
            }
        }, 0, 60000); // Clean up every minute
    }

    public String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void cleanupOldEntries() {
        // Remove entries older than 5 minutes
        long fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000);
        // You can add timestamp tracking if needed
    }

    // In your existing MatchService, update the createMatch method
    public Optional<Long> createMatch(HttpServletRequest request, CreateMatchRequest createMatchRequest) {
        String tk = getJwtFromCookie(request);
        String userName = jwtService.extractUsername(tk);

        if (userName == null) {
            return Optional.empty();
        }

        GameType requestedGameType = createMatchRequest != null && createMatchRequest.getGameType() != null 
                ? createMatchRequest.getGameType() 
                : GameType.STANDARD;

        System.out.println("User " + userName + " requesting match with game type: " + requestedGameType);

        synchronized(this) {
            // Check if there's already a waiting player with the same game type
            for (Map.Entry<String, GameType> entry : waitingPlayers.entrySet()) {
                String waitingPlayer = entry.getKey();
                GameType waitingGameType = entry.getValue();
                
                if (!waitingPlayer.equals(userName) && waitingGameType == requestedGameType) {
                    // Found opponent with matching game type
                    User player1 = userRepo.getUserByUsername(waitingPlayer);
                    User player2 = userRepo.getUserByUsername(userName);

                    if (player1 != null && player2 != null) {
                        // Create the match with game type
                        Match newMatch = new Match(player1, player2, IN_PROGRESS, 1);
                        newMatch.setGameType(requestedGameType);
                        
                        // Set initial time based on game type
                        setInitialTimeForMatch(newMatch, requestedGameType);
                        
                        newMatch = matchRepo.save(newMatch);
                        Long matchId = newMatch.getId();

                        // Store match info
                        matchPlayers.put(matchId, new String[]{waitingPlayer, userName});

                        // Remove waiting player
                        waitingPlayers.remove(waitingPlayer);

                        System.out.println("Match created: " + matchId + " with game type: " + requestedGameType);

                        // Initialize game state
                        gameService.getGameDetails(matchId, request);

                        return Optional.of(matchId);
                    }
                }
            }

            // No opponent found, add to waiting queue with game type
            waitingPlayers.put(userName, requestedGameType);
            System.out.println("User " + userName + " added to waiting queue for game type: " + requestedGameType);

            return Optional.of(-1L);
        }
    }

    private void setInitialTimeForMatch(Match match, GameType gameType) {
        switch (gameType) {
            case BLITZ:
                // 3 minutes = 180 seconds
                match.setPlayer1TimeRemaining(180);
                match.setPlayer2TimeRemaining(180);
                break;
            case RAPID:
                // 10 minutes = 600 seconds
                match.setPlayer1TimeRemaining(600);
                match.setPlayer2TimeRemaining(600);
                break;
            case STANDARD:
            default:
                // No time limit
                match.setPlayer1TimeRemaining(null);
                match.setPlayer2TimeRemaining(null);
                break;
        }
    }

    // Method for Player1 to check if match was created
    public Optional<Long> checkMatch(HttpServletRequest request) {
        String tk = getJwtFromCookie(request);
        String userName = jwtService.extractUsername(tk);

        if (userName == null) {
            return Optional.empty();
        }

        synchronized(this) {
            // Check if user is in waitingPlayers (regardless of game type)
            if (waitingPlayers.containsKey(userName)) {
                // User is still waiting
                return Optional.of(-1L);
            }

            // Check if user has a match in matchPlayers
            for (Map.Entry<Long, String[]> entry : matchPlayers.entrySet()) {
                String[] players = entry.getValue();
                if (players[0].equals(userName) || players[1].equals(userName)) {
                    Long matchId = entry.getKey();
                    // Clean up after retrieving
                    matchPlayers.remove(matchId);
                    waitingPlayers.remove(players[0]);
                    waitingPlayers.remove(players[1]);
                    System.out.println("Returning match " + matchId + " to " + userName);
                    return Optional.of(matchId);
                }
            }
        }

        return Optional.empty();
    }

    // Method to cancel waiting
    public boolean cancelWaiting(HttpServletRequest request) {
        String tk = getJwtFromCookie(request);
        String userName = jwtService.extractUsername(tk);

        if (userName == null) {
            return false;
        }

        synchronized(this) {
            boolean removed = waitingPlayers.remove(userName) != null;
            if (removed) {
                System.out.println("User " + userName + " cancelled waiting");
            }
            return removed;
        }
    }

    private Map<String, Object> createPlayerInfo(User user) {
        Map<String, Object> playerInfo = new HashMap<>();
        playerInfo.put("id", user.getUserId());
        playerInfo.put("username", user.getUsername());
        // Add other user info if needed
        return playerInfo;
    }

    private boolean determineIfMyTurn(Match match, boolean isPlayer1) {
        // Simple logic: if currentPly is even, it's white's turn
        // You may need more sophisticated logic based on your game state
        Integer currentPly = match.getCurrentPly();
        if (currentPly == null) {
            currentPly = 0;
        }

        // Even ply = white's turn, odd ply = black's turn
        boolean isWhiteTurn = currentPly % 2 == 0;

        // Player1 is white, Player2 is black
        return (isPlayer1 && isWhiteTurn) || (!isPlayer1 && !isWhiteTurn);
    }

    public Map<String, Object> getGameDetailsForFrontend(Long matchId, HttpServletRequest request) {
        String token = getJwtFromCookie(request);
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        String username = jwtService.extractUsername(token);
        if (username == null) {
            throw new RuntimeException("Invalid token");
        }

        // Find the match
        Optional<Match> matchOpt = matchRepo.findById(matchId);
        if (matchOpt.isEmpty()) {
            throw new RuntimeException("Game not found");
        }

        Match match = matchOpt.get();

        // Check if user is part of this match
        User player1 = match.getPlayer1();
        User player2 = match.getPlayer2();

        boolean isPlayer1 = player1.getUsername().equals(username);
        boolean isPlayer2 = player2 != null && player2.getUsername().equals(username);

        if (!isPlayer1 && !isPlayer2) {
            throw new RuntimeException("Not authorized to view this game");
        }

        // Determine player color
        String playerColor = isPlayer1 ? "white" : "black";

        // Determine if it's this player's turn
        // For initial implementation: white starts, then alternate based on currentPly
        boolean isMyTurn = determineIfMyTurn(match, isPlayer1);

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("matchId", match.getId());
        response.put("player1", createPlayerInfo(player1));

        if (player2 != null) {
            response.put("player2", createPlayerInfo(player2));
        }

        response.put("status", match.getStatus() != null ? match.getStatus().toString() : "IN_PROGRESS");
        response.put("playerColor", playerColor);
        response.put("isMyTurn", isMyTurn);
        response.put("createdAt", match.getCreatedAt());
        response.put("startedAt", match.getStartedAt());
        response.put("currentPly", match.getCurrentPly());
        response.put("fenCurrent", match.getFenCurrent());
        response.put("gameType", match.getGameType() != null ? match.getGameType().toString() : "STANDARD");
        response.put("player1TimeRemaining", match.getPlayer1TimeRemaining());
        response.put("player2TimeRemaining", match.getPlayer2TimeRemaining());

        return response;
    }
}
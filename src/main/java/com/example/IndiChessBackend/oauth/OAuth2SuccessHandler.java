package com.example.IndiChessBackend.oauth;

import com.example.IndiChessBackend.model.User;
import com.example.IndiChessBackend.repo.UserRepo;
import com.example.IndiChessBackend.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepo userRepo;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        // Extract OAuth details
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        if (email == null) {
            throw new RuntimeException("Email not found from OAuth provider");
        }

        // Find or create user
        User user = userRepo.getUserByEmailId(email);
        if (user == null) {
            user = new User();
            user.setEmailId(email);
            user.setUsername(name != null ? name : email);
            userRepo.save(user);
        }

        // ✅ Generate JWT using USERNAME (IMPORTANT)
        String jwt = jwtService.generateToken(user.getUsername());

        // Store JWT in HttpOnly cookie
        Cookie jwtCookie = new Cookie("JWT", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // ✅ false for localhost (true in production HTTPS)
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60); // 1 hour

        response.addCookie(jwtCookie);

        // ✅ Redirect to React frontend (NOT backend)
        response.sendRedirect("http://localhost:3000/home");
    }
}

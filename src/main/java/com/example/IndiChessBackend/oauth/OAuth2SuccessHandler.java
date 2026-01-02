package com.example.IndiChessBackend.oauth;


import com.example.IndiChessBackend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;



    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        String subject;

        Object principal = authentication.getPrincipal();

        // Google (OIDC) -> email is usually present
        if (principal instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser) {
            subject = oidcUser.getEmail(); // or oidcUser.getSubject()
        } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
            // Twitter/X -> depends on userinfo mapping; often id or username
            Object id = oAuth2User.getAttributes().get("id");
            subject = (id != null) ? id.toString() : authentication.getName();
        } else {
            subject = authentication.getName();
        }

        String jwt = jwtService.generateToken(subject);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write("{\"token\":\"" + jwt + "\"}");
        response.getWriter().flush();
    }
}
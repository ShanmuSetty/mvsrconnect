package com.mvsr.mvsrconnect.config;

import com.mvsr.mvsrconnect.model.User;
import com.mvsr.mvsrconnect.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");

        String hd = oauthUser.getAttribute("hd");

        if (hd == null || !hd.equals("mvsrec.edu.in")) {

            request.getSession().invalidate();
            response.sendRedirect("/?error=unauthorized");
            return;
        }

        // Create user in DB if first time login
        userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setGoogleId(oauthUser.getAttribute("sub"));
            newUser.setName(oauthUser.getAttribute("name"));
            newUser.setEmail(email);
            newUser.setPicture(oauthUser.getAttribute("picture"));
            return userRepository.save(newUser);
        });

        response.sendRedirect("/");
    }
}
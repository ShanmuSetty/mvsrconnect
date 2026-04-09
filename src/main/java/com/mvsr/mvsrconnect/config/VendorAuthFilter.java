package com.mvsr.mvsrconnect.config;

import com.mvsr.mvsrconnect.model.VendorSession;
import com.mvsr.mvsrconnect.repository.VendorSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class VendorAuthFilter extends OncePerRequestFilter {

    @Autowired
    private VendorSessionRepository vendorSessionRepo;

    private static final String VENDOR_PREFIX = "/canteen/vendor/";
    // These paths under /canteen/vendor/ are public (no cookie needed)
    private static final String LOGIN_PATH = "/canteen/vendor/login";
    private static final String DASHBOARD_PATH = "/canteen/vendor/dashboard";

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        String uri = req.getRequestURI();

        // Only intercept /canteen/vendor/** API calls
        if (!uri.startsWith(VENDOR_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        // Let static pages and login through — API calls need the cookie
        if (uri.equals(LOGIN_PATH) || uri.equals(DASHBOARD_PATH) || uri.endsWith(".html")) {
            chain.doFilter(req, res);
            return;
        }

        String token = extractCookie(req, "vendorSession");

        if (token == null) {
            sendUnauth(req, res);
            return;
        }

        Optional<VendorSession> session = vendorSessionRepo.findBySessionToken(token);
        if (session.isEmpty() || session.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            sendUnauth(req, res);
            return;
        }

        req.setAttribute("vendorStallId", session.get().getStallId());
        chain.doFilter(req, res);
    }

    private void sendUnauth(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // API calls get 401; page navigations get redirect
        if (req.getRequestURI().startsWith("/canteen/vendor/api/")) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Vendor session expired");
        } else {
            res.sendRedirect("/canteen-vendor-login.html");
        }
    }

    private String extractCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}

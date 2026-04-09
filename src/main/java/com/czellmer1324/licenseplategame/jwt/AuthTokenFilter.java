package com.czellmer1324.licenseplategame.jwt;

import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.services.UserAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    private final JwtUtils utils;
    private final UserAuthService authService;

    public AuthTokenFilter(JwtUtils utils, UserAuthService authService) {
        this.utils = utils;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // get the token from the header
            String token = utils.getTokenFromHeader(request);

            // Make sure the token is not null and it is valid
            if (token != null && utils.verifyToken(token)) {
                // Get the user using the user ID
                User user = authService.getUserByID(utils.getIdFromToken(token));
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
                        Collections.emptyList());

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            log.info("Something went wrong");
            log.info(e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

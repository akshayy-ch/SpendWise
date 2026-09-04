package com.spendwise.security;

import com.spendwise.service.SpendWiseUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SpendWiseUserDetailsService spendWiseUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        System.out.println("AUTH HEADER: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("NO BEARER TOKEN");
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        String username = jwtService.extractUsername(jwt);

        System.out.println("USERNAME FROM TOKEN: " + username);

        if (username == null) {
            System.out.println("TOKEN COULD NOT BE PARSED");
            filterChain.doFilter(request, response);
            return;
        }

        boolean valid = jwtService.isTokenValid(jwt, username);

        System.out.println("TOKEN VALID: " + valid);

        if (valid &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails =
                    spendWiseUserDetailsService.loadUserByUsername(username);

            System.out.println("USER FOUND: " + userDetails.getUsername());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);

            System.out.println("AUTHENTICATION SET");
        }

        filterChain.doFilter(request, response);
    }
}

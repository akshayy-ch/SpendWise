package com.spendwise.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService{

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username){
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public Claims getClaims(String token) {
        try {
            return extractAllClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
    public String extractUsername(String token) {
        Claims claims = getClaims(token);

        if (claims == null) {
            return null;
        }

        return claims.getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        Claims claims = getClaims(token);
        if(claims == null)return false;
        return (username.equals(claims.getSubject()) && !claims.getExpiration().before(new Date()));
    }
}

package com.sales.sales.Services;

import com.sales.sales.Entity.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    String generateToken(User user);
    String generateToken(User user, Map<String, Object> claims);
    String generateRefreshToken(User user);
    String extractUsername(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    boolean isTokenValid(String token, UserDetails userDetails);
    boolean isTokenExpired(String token);
    Claims extractAllClaims(String token);
}
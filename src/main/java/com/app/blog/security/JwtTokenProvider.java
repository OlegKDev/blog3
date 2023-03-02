package com.app.blog.security;

import com.app.blog.exception.BlogApiException;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private int jwtExpirationInMs;

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + jwtExpirationInMs);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        return token;
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        return username;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token);

        } catch (SignatureException ex) {
            String message = "Invalid Jwt signature";
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            throw new BlogApiException(message, status);

        } catch (MalformedJwtException ex) {
            String message = "Invalid Jwt token";
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            throw new BlogApiException(message, status);

        } catch (ExpiredJwtException ex) {
            String message = "Expired Jwt token";
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            throw new BlogApiException(message, status);

        } catch (UnsupportedJwtException ex) {
            String message = "Unsupported Jwt token";
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            throw new BlogApiException(message, status);

        } catch (IllegalArgumentException ex) {
            String message = "Jwt's claims string is empty";
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            throw new BlogApiException(message, status);
        }

        return true;
    }
}

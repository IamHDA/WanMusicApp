package com.example.backend.security;

import com.example.backend.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret-string}")
    private String secretString;
    private final TokenRepository tokenRepo;

    private SecretKey decodeSecretString(){
        byte[] decodedKey = Decoders.BASE64URL.decode(secretString);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    private String generateKey(UserDetails userDetails, long expirationTime){
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .signWith(decodeSecretString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public String generateAccessToken(UserDetails userDetails){
        return generateKey(userDetails, 1000 * 6);
    }

    public String generateRefreshToken(UserDetails userDetails){
        return generateKey(userDetails, 1000 * 60 * 60 * 24);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(decodeSecretString())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateAccessToken(String token, UserDetails userDetails){
        String tokenSubject = extractSubject(token);
        return tokenSubject.equals(userDetails.getUsername()) && !isTokenExpired(token) && tokenRepo.findByAccessTokenAndLoggedOutFalse(token).isPresent();
    }

    public boolean validateRefreshToken(String token, UserDetails userDetails){
        String tokenSubject = extractSubject(token);
        return tokenSubject.equals(userDetails.getUsername()) && !isTokenExpired(token) && tokenRepo.findByRefreshTokenAndLoggedOutFalse(token).isPresent();
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public String extractSubject(String token){
        return extractAllClaims(token).getSubject();
    }

    private Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }
}

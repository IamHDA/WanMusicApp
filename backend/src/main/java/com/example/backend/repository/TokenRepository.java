package com.example.backend.repository;

import com.example.backend.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByAccessTokenAndLoggedOutFalse(String accessToken);
    Optional<Token> findByRefreshTokenAndLoggedOutFalse(String refreshToken);
}

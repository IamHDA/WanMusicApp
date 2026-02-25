package com.example.backend.service.implement;

import com.example.backend.Enum.Role;
import com.example.backend.Enum.SubscriptionType;
import com.example.backend.Enum.UserStatus;
import com.example.backend.dto.authentication.LogInRequest;
import com.example.backend.dto.authentication.RegisterRequest;
import com.example.backend.dto.authentication.AuthenticationResponse;
import com.example.backend.entity.Token;
import com.example.backend.entity.User;
import com.example.backend.repository.TokenRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtTokenProvider;
import com.example.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImp implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepo;
    private final TokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public AuthenticationResponse login(LogInRequest request) {
        if(userRepo.findByEmail(request.getEmail()).isEmpty()){
            return new AuthenticationResponse(null, null, "Account not found!");
        }

        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        }catch (BadCredentialsException e){
            return new AuthenticationResponse(null, null, "Invalid credentials!");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        saveToken(request.getEmail(), accessToken, refreshToken);

        userRepo.updateUserStatus(request.getEmail(), UserStatus.ONLINE);

        return new AuthenticationResponse(accessToken, refreshToken, "Logged in successfully!");
    }

    @Transactional
    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        if(userRepo.findByEmail(request.getEmail()).isPresent()){
            return new AuthenticationResponse(null, null, "Account already exists!");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.USER);

        userRepo.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        saveToken(request.getEmail(), accessToken, refreshToken);

        return new AuthenticationResponse(accessToken, refreshToken, "Account created successfully!");
    }

    private void saveToken(String email, String accessToken, String refreshToken){
        tokenRepo.logoutAllTokensByEmail(email);

        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(userRepo.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found!")));

        tokenRepo.save(token);
    }
}

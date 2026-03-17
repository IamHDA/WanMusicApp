package com.example.backend.service.implement;

import com.example.backend.Enum.Role;
import com.example.backend.Enum.SubscriptionType;
import com.example.backend.Enum.UserStatus;
import com.example.backend.dto.authentication.LogInRequest;
import com.example.backend.dto.authentication.RegisterRequest;
import com.example.backend.dto.authentication.AuthenticationResponse;
import com.example.backend.entity.Member;
import com.example.backend.entity.Token;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.TokenRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtTokenProvider;
import com.example.backend.security.UserPrinciple;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImp implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepo;
    private final MemberRepository memberRepo;
    private final TokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AuthenticationResponse login(LogInRequest request) {
        Optional<Member> member = memberRepo.findByEmail(request.email());

        if(member.isEmpty()){
            return new AuthenticationResponse(null, null, null, "Account not found!");
        }

        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        }catch (BadCredentialsException e){
            return new AuthenticationResponse(null, null, null, "Invalid credentials!");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        saveToken(request.email(), accessToken, refreshToken);

        memberRepo.updateUserStatus(request.email(), UserStatus.ONLINE);

        return new AuthenticationResponse(member.get().getId(), accessToken, refreshToken, "Logged in successfully!");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        if(userRepo.findByEmail(request.email()).isPresent()){
            return new AuthenticationResponse(null, null, null, "Account already exists!");
        }
        Member member = new Member();
        member.setEmail(request.email());
        member.setPassword(passwordEncoder.encode(request.password()));
        member.setCreatedAt(LocalDateTime.now());
        member.setRole(Role.USER);
        member.setSubscriptionType(SubscriptionType.FREE);
        member.setFullName(request.displayName());
        member.setStatus(UserStatus.ONLINE);
        member.setAvatarKey("avatar.png");

        memberRepo.save(member);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        saveToken(request.email(), accessToken, refreshToken);

        return new AuthenticationResponse(member.getId(),accessToken, refreshToken, "Account created successfully!");
    }

    @Override
    public Long getCurrentMemberId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
        return userPrinciple.getId();
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

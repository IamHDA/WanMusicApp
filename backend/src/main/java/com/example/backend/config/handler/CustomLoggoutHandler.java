package com.example.backend.config.handler;

import com.example.backend.Enum.UserStatus;
import com.example.backend.entity.Member;
import com.example.backend.entity.Token;
import com.example.backend.entity.User;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.TokenRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.UserPrinciple;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomLoggoutHandler implements LogoutHandler {

    private final MemberRepository memberRepo;
    private final UserRepository userRepo;
    private final TokenRepository tokenRepo;


    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")) return;

        String accessToken = authHeader.substring(7);
        Long userId;

        if(!(authentication instanceof AnonymousAuthenticationToken)){
            UserPrinciple user = (UserPrinciple) authentication.getPrincipal();
            userId = user.getId();
        }else{
            return;
        }

        Member member = memberRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found!"));
        member.setStatus(UserStatus.OFFLINE);

        Token storedToken = tokenRepo.findByAccessToken(accessToken).orElseThrow(() -> new RuntimeException("Token not found!"));
        storedToken.setLoggedOut(true);
    }
}

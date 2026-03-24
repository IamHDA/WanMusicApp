package com.example.backend.service.implement;

import com.example.backend.repository.FollowerRepository;
import com.example.backend.service.FollowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowerServiceImp implements FollowerService {

    private final FollowerRepository followerRepo;

    @Override
    public int countFollowedArtistByUserId(Long userId) {
        return followerRepo.countByFollower_Id(userId);
    }
}

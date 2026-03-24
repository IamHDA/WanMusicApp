package com.example.backend.repository;

import com.example.backend.entity.EmbeddedId.FollowerId;
import com.example.backend.entity.Follower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowerRepository extends JpaRepository <Follower, FollowerId>{
    int countByFollower_Id(Long followerId);
    int countByArtist_Id(Long artistId);
}

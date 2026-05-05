package com.example.backend.repository;

import com.example.backend.entity.EmbeddedId.FollowerId;
import com.example.backend.entity.Follower;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowerRepository extends JpaRepository <Follower, FollowerId>{
    @Query("select count(f) from Follower f where f.follower.id = :followerId")
    int countByFollower_Id(Long followerId);

    @Query("select count(f) from Follower f where f.artist.id = :artistId")
    int countByArtist_Id(Long artistId);
    void deleteByFollower_IdAndArtist_Id(Long followerId, Long artistId);
    Page<Follower> findByFollower_IdAndArtist_Id(Long followerId, Long artistId, Pageable pageable);
    boolean existsByFollower_IdAndArtist_Id(Long currentUserId, Long artistId);
}

package com.example.backend.repository;

import com.example.backend.entity.EmbeddedId.FriendshipId;
import com.example.backend.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FriendshipRepository extends JpaRepository <Friendship, FriendshipId> {
    @Query(
            "select count(f) from Friendship f " +
            "where f.friend.id = :userId or f.member.id = :userId"
    )
    int countFriendByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(
            "delete from Friendship f " +
            "where (f.friend.id = :memberId and f.member.id = :friendId) " +
            "   or (f.friend.id = :friendId and f.member.id = :memberId) "
    )
    Friendship deleteByMemberIdAndFriendId(@Param("memberId") Long memberId, @Param("friendId") Long friendId);
}

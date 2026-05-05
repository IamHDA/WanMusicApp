package com.example.backend.repository;

import com.example.backend.entity.EmbeddedId.FriendshipId;
import com.example.backend.entity.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository <Friendship, FriendshipId> {
    @Query(
            "select count(f) from Friendship f " +
            "where (f.friend.id = :userId or f.member.id = :userId) " +
            "and f.status = 'ACCEPTED'"
    )
    int countFriendByUserId(@Param("userId") Long userId);

    @Query(
            "select f from Friendship f " +
            "where (f.friend.id = :memberId and f.member.id = :friendId) " +
                    "or (f.friend.id = :friendId and f.member.id = :memberId)"
    )
    Friendship findByMemberIdAndFriendId(@Param("memberId") Long memberId, @Param("friendId") Long friendId);

    @Query(
            """
    select f from Friendship f
    where f.friend.id = :memberId or f.member.id = :memberId
"""
    )
    Page<Friendship> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Modifying
    @Query(
            "delete from Friendship f " +
            "where (f.friend.id = :memberId and f.member.id = :friendId) " +
            "   or (f.friend.id = :friendId and f.member.id = :memberId) "
    )
    void deleteByMemberIdAndFriendId(@Param("memberId") Long memberId, @Param("friendId") Long friendId);
}

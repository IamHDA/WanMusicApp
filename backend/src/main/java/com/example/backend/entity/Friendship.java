package com.example.backend.entity;

import com.example.backend.Enum.FriendStatus;
import com.example.backend.entity.EmbeddedId.FriendshipId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Friendship {

    @EmbeddedId
    private FriendshipId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @MapsId("friendId")
    @JoinColumn(name = "friend_id", nullable = false)
    private Member friend;

    public Friendship(Member member, Member friend){
        this.member = member;
        this.friend = friend;
    }

}

package com.example.backend.entity.EmbeddedId;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FriendshipId implements Serializable {

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "friend_id")
    private Long friendId;
}

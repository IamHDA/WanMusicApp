package com.example.backend.entity;

import java.time.LocalDateTime;

import com.example.backend.entity.EmbeddedId.UserTagPreferenceId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_tag_preference")
@Getter
@Setter
@NoArgsConstructor
public class UserTagPreference {

    @EmbeddedId
    private UserTagPreferenceId id;
    @Column(nullable = false)
    private long score;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;

     public UserTagPreference(Member member, Tag tag, int score){
        this.member = member;
        this.tag = tag;
        this.score = score;
    }

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

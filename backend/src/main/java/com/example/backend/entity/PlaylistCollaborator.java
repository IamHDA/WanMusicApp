package com.example.backend.entity;

import com.example.backend.Enum.CollaboratorPermission;
import com.example.backend.entity.EmbeddedId.PlaylistCollabId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "playlist_collaborator")
@Getter
@Setter
@NoArgsConstructor
public class PlaylistCollaborator {

    @EmbeddedId
    private PlaylistCollabId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "collaborator_permission", nullable = false)
    private Set<CollaboratorPermission> collabPermission;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("collaboratorId")
    @JoinColumn(name = "collaborator_id")
    private Member collaborator;

    public PlaylistCollaborator(Playlist playlist, Member collaborator){
        this.playlist = playlist;
        this.collaborator = collaborator;
    }
}

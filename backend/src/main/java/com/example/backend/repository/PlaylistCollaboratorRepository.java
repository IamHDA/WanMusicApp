package com.example.backend.repository;

import com.example.backend.dto.user.UserPreviewDTO;
import com.example.backend.entity.EmbeddedId.PlaylistCollabId;
import com.example.backend.entity.PlaylistCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistCollaboratorRepository extends JpaRepository<PlaylistCollaborator, PlaylistCollabId> {
    Optional<PlaylistCollaborator> findByPlaylist_IdAndCollaborator_Id(Long id, Long currentMemberId);

    List<PlaylistCollaborator> findByPlaylist_Id(Long playlistId);
}

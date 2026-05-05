package com.example.backend.repository;

import com.example.backend.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    @Query("""
        select distinct p from Playlist p
        left join PlaylistCollaborator pc on p.id = pc.playlist.id
        where p.owner.id = :memberId or pc.collaborator.id = :memberId
    """)
    Optional<List<Playlist>> findMemberPlaylists(@Param("memberId") Long memberId);

    int countByOwnerId(Long ownerId);
}

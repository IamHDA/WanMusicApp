package com.example.backend.repository;

import com.example.backend.Enum.ContributorRole;
import com.example.backend.entity.ArtistContribution;
import com.example.backend.entity.EmbeddedId.ArtistContributionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistContributionRepository extends JpaRepository<ArtistContribution, ArtistContributionId> {
    List<ArtistContribution> findByContributor_IdAndRole(Long artistId, ContributorRole contributorRole);
}

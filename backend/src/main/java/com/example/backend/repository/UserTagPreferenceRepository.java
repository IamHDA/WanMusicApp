package com.example.backend.repository;

import com.example.backend.entity.EmbeddedId.UserTagPreferenceId;
import com.example.backend.entity.UserTagPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTagPreferenceRepository extends JpaRepository<UserTagPreference, UserTagPreferenceId> {
}
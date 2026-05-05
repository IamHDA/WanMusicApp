package com.example.backend.repository;

import com.example.backend.Enum.UserStatus;
import com.example.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Modifying
    @Query("update Member m set m.status = :status where m.email = :email")
    void updateUserStatus(@Param("email") String email, @Param("status") UserStatus status);

    Optional<Member> findByEmail(String name);
}

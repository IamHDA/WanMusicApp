package com.example.backend.repository;

import com.example.backend.dto.track.TrackStatisticDTO;
import com.example.backend.entity.Track;
import com.example.backend.entity.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    @Query("SELECT COUNT(ui) as playCount, SUM(ui.duration) as listenDuration, " +
            "(SELECT COUNT(ui1) FROM UserInteraction ui1 where ui1.type = 'SAVED' and ui1.track.id = :trackId) as favouriteCount, " +
            "(SELECT COUNT(ui1) FROM UserInteraction ui1 where ui1.type = 'JAM' and ui1.track.id = :trackId) as jamCount, " +
            "(SELECT COUNT(ui1) FROM UserInteraction ui1 where ui1.type = 'SHARE' and ui1.track.id =: trakcId) as shareCount " +
            "FROM UserInteraction ui WHERE ui.type = 'PLAY' and ui.track.id = :trackId")
    TrackStatisticDTO findByTrackId(@Param("trackId") Long trackId);

    @Query("SELECT ui.track FROM UserInteraction ui " +
            "WHERE MONTH(ui.time) = :month and ui.type = 'PLAY' " +
            "GROUP BY ui.track " +
            "ORDER BY SUM(ui.duration) DESC")
    List<Track> findTopTracksInMonth(@Param("month") int month);

}

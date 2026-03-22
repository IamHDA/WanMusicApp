package com.example.backend.service;

import com.example.backend.Enum.InterationType;
import com.example.backend.entity.Member;
import com.example.backend.entity.Track;

public interface UserInteractionService {

    void addInteraction(Member member, Track track, InterationType type);

}

package com.example.backend.service.implement;

import com.example.backend.dto.SharePlaylistRequestDTO;
import com.example.backend.entity.Member;
import com.example.backend.entity.Playlist;
import com.example.backend.entity.SharedPlaylist;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.PlaylistRepository;
import com.example.backend.repository.SharedPlaylistRepository;
import com.example.backend.service.SharedPlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SharedPlaylistServiceImp implements SharedPlaylistService {

    private final SharedPlaylistRepository sharedPlaylistRepo;
    private final MemberRepository memberRepo;
    private final PlaylistRepository playlistRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sharePlaylist(SharePlaylistRequestDTO dto) {
        Member member = memberRepo.findById(dto.sharedMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));
        Playlist playlist = playlistRepo.findById(dto.playlistId()).orElseThrow(()-> new RuntimeException("Playlist not found!"));
        SharedPlaylist sharedPlaylist = new SharedPlaylist(playlist, member);

        sharedPlaylistRepo.save(sharedPlaylist);
        return "Playlist shared successfully!";
    }

}

package com.example.backend.service.implement;

import com.example.backend.dto.SearchRequestDTO;
import com.example.backend.dto.SearchResponseDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.user.UserPreviewDTO;
import com.example.backend.repository.SearchRepository;
import com.example.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchServiceImp implements SearchService {

    private final SearchRepository searchRepo;

    @Override
    public SearchResponseDTO search(SearchRequestDTO searchRequestDTO) {
        SearchResponseDTO dto = searchRepo.search(searchRequestDTO);
        for(UserPreviewDTO item : dto.getMemberPreviewDTOS().getContent())
            System.out.println(item.name());
        return dto;
    }
}

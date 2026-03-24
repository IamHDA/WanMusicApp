package com.example.backend.service.implement;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.SearchRequestDTO;
import com.example.backend.dto.SearchResponseDTO;
import com.example.backend.dto.user.UserPreviewDTO;
import com.example.backend.mapper.PageMapper;
import com.example.backend.repository.SearchRepository;
import com.example.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchServiceImp implements SearchService {

    private final SearchRepository searchRepo;
    private final PageMapper pageMapper;

    @Override
    public SearchResponseDTO search(SearchRequestDTO searchRequestDTO) {
        return searchRepo.search(searchRequestDTO);
    }

    @Override
    public PageResponse<UserPreviewDTO> searchFriends(String query, int pageNumber) {
        return pageMapper.toPageResponse(searchRepo.searchFriends(query, pageNumber));
    }
}

package com.example.backend.service.implement;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.SearchRequestDTO;
import com.example.backend.dto.SearchResponseDTO;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import com.example.backend.dto.user.UserPreviewDTO;
import com.example.backend.mapper.PageMapper;
import com.example.backend.repository.SearchRepository;
import com.example.backend.service.RedisService;
import com.example.backend.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchServiceImp implements SearchService {

    private final SearchRepository searchRepo;
    private final PageMapper pageMapper;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Override
    public SearchResponseDTO search(SearchRequestDTO searchRequestDTO) {
        String key = "/search/" + searchRequestDTO.keyword() + "/" + searchRequestDTO.type() + "/" + searchRequestDTO.pageNumber() + "/" + searchRequestDTO.pageSize();
        SearchResponseDTO data = null;
        if(redisService.hasKey(key))
            return objectMapper.convertValue(redisService.get(key), SearchResponseDTO.class);

        data = searchRepo.search(searchRequestDTO);
        redisService.save(key, data, 60);

        return data;
    }

    @Override
    public PageResponse<MemberProfilePreviewDTO> searchFriends(String query, int pageNumber) {

        return pageMapper.toPageResponse(searchRepo.searchFriends(query, pageNumber));
    }
}

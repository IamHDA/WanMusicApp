package com.example.backend.service;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.SearchRequestDTO;
import com.example.backend.dto.SearchResponseDTO;
import com.example.backend.dto.user.MemberProfilePreviewDTO;

public interface SearchService {
    SearchResponseDTO search(SearchRequestDTO searchRequestDTO);
    PageResponse<MemberProfilePreviewDTO> searchFriends(String query, int pageNumber);
}

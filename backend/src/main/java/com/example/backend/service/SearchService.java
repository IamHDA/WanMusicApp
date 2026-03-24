package com.example.backend.service;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.SearchRequestDTO;
import com.example.backend.dto.SearchResponseDTO;
import com.example.backend.dto.user.UserPreviewDTO;

public interface SearchService {
    SearchResponseDTO search(SearchRequestDTO searchRequestDTO);
    PageResponse<UserPreviewDTO> searchFriends(String query, int pageNumber);
}

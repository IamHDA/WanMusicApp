package com.example.backend.service;

import com.example.backend.dto.SearchRequestDTO;
import com.example.backend.dto.SearchResponseDTO;

public interface SearchService {
    SearchResponseDTO search(SearchRequestDTO searchRequestDTO);
}

package com.example.backend.controller;

import com.example.backend.dto.SearchRequestDTO;
import com.example.backend.dto.SearchResponseDTO;
import com.example.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponseDTO> search(@RequestParam String keyword, @RequestParam String type, @RequestParam int pageNumber, @RequestParam int pageSize){
        return ResponseEntity.ok(searchService.search(SearchRequestDTO.builder()
                .keyword(keyword)
                .type(type)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build()));
    }

}

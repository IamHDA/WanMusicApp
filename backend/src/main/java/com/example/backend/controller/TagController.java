package com.example.backend.controller;

import com.example.backend.dto.CreateTagRequestDTO;
import com.example.backend.dto.TagDTO;
import com.example.backend.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/tags")
    public ResponseEntity<List<TagDTO>> getAllTags(){
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping("/createTag")
    @PreAuthorize( "hasRole('ADMIN')")
    public ResponseEntity<String> createTag(@RequestBody CreateTagRequestDTO dto){
        return ResponseEntity.ok(tagService.createTag(dto));
    }

    @PutMapping("/updateTag")
    @PreAuthorize( "hasRole('ADMIN')")
    public ResponseEntity<String> updateTag(@RequestParam Long id,@RequestBody CreateTagRequestDTO dto){
        return ResponseEntity.ok(tagService.updateTag(id, dto));
    }

    @GetMapping("/getTag")
    @PreAuthorize( "hasRole('ADMIN')")
    public ResponseEntity<TagDTO> getTab(@RequestParam Long id){
        return ResponseEntity.ok(tagService.getTag(id));
    }

    @DeleteMapping("/deleteTag")
    @PreAuthorize( "hasRole('ADMIN')")
    public ResponseEntity<String> deleteTag(@RequestParam Long id){
        return ResponseEntity.ok(tagService.deleteTag(id));
    }

}

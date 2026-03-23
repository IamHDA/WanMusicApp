package com.example.backend.service;

import com.example.backend.dto.CreateTagRequestDTO;
import com.example.backend.dto.TagDTO;

import java.util.List;

public interface TagService {

    String createTag(CreateTagRequestDTO dto);
    List<TagDTO> getAllTags();
    String deleteTag(Long tagId);

}

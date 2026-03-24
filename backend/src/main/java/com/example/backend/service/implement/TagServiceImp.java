package com.example.backend.service.implement;

import com.example.backend.dto.CreateTagRequestDTO;
import com.example.backend.dto.TagDTO;
import com.example.backend.entity.Tag;
import com.example.backend.mapper.TagMapper;
import com.example.backend.repository.TagRepository;
import com.example.backend.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagServiceImp implements TagService {

    private final TagRepository tagRepo;
    private final TagMapper tagMapper;

    @Override
    public String createTag(CreateTagRequestDTO dto) {
        Tag tag = new Tag();
        Optional<Tag> parentTag = tagRepo.findById(dto.parentTagId());

        parentTag.ifPresent(tag::setParentTags);

        tag.setName(dto.name());
        tag.setDescription(dto.description());

        return "Tag created successfully!";
    }

    @Override
    public List<TagDTO> getAllTags() {
        return tagRepo.findAll().stream()
                .map(tagMapper::toDTO)
                .toList();
   }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteTag(Long tagId) {
        tagRepo.deleteById(tagId);
        return "Tag deleted successfully!";
    }
}

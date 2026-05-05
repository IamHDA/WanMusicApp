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
    @Transactional(rollbackFor = Exception.class)
    public TagDTO createTag(CreateTagRequestDTO dto) {
        Tag tag = new Tag();
        if (dto.parentTagId() != null && dto.parentTagId() != 0) {
            Optional<Tag> parentTag = tagRepo.findById(dto.parentTagId());
            parentTag.ifPresent(tag::setParentTags);
        }

        tag.setName(dto.name());
        tag.setDisplayName(dto.displayName());
        tag.setDescription(dto.description());
        tagRepo.save(tag);

        tagRepo.save(tag);

        return tagMapper.toDTO(tag);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateTag(Long id, CreateTagRequestDTO dto) {
        Tag tag = tagRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found!"));

        tag.setName(dto.name());
        tag.setDisplayName(dto.displayName());
        tag.setDescription(dto.description());

        if (dto.parentTagId() != null && dto.parentTagId() != 0) {
            Optional<Tag> parentTag = tagRepo.findById(dto.parentTagId());
            parentTag.ifPresent(tag::setParentTags);
        } else {
            tag.setParentTags(null); // Fix lỗi không xoá được Parent Tag (update không ăn)
        }
        tagRepo.save(tag);

        return "Tag updated successfully!";
    }

    @Override
    public TagDTO getTag(Long id) {
        Tag tag = tagRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found!"));

        return tagMapper.toDTO(tag);
    }
}

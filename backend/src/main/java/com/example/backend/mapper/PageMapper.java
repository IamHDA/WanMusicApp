package com.example.backend.mapper;

import com.example.backend.dto.PageResponse;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.function.Function;

@Mapper(componentModel = "spring")
public interface PageMapper {

    default <E, D> PageResponse<D> toPageResponse(
            Page<E> page,
            Function<E, D> mapper
    ) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(mapper)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    default <E> PageResponse<E> toPageResponse(Page<E> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}

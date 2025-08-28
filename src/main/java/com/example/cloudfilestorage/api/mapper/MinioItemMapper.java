package com.example.cloudfilestorage.api.mapper;

import com.example.cloudfilestorage.api.dto.ResourcesResponse;
import com.example.cloudfilestorage.core.model.Resource;
import io.minio.messages.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MinioItemMapper {
    Item toEntity(ResourcesResponse resourcesResponse);

    ResourcesResponse toDTO(Item minioItem);
}

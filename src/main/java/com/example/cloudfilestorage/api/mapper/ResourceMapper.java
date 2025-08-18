package com.example.cloudfilestorage.api.mapper;


import com.example.cloudfilestorage.api.dto.UploadResourcesResponse;
import com.example.cloudfilestorage.core.model.Resource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResourceMapper {

    Resource toEntity(UploadResourcesResponse uploadResourcesResponse);

    UploadResourcesResponse toDTO(Resource resource);
}

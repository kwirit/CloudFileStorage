package com.example.cloudfilestorage.api.mapper;


import com.example.cloudfilestorage.api.dto.ResourcesResponse;
import com.example.cloudfilestorage.core.model.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ResourceMapper {
    @Mapping(source = "name", target = "fileName")
    @Mapping(source = "path", target = "filePath")
    Resource toEntity(ResourcesResponse resourcesResponse);

    @Mapping(source = "fileName", target = "name")
    @Mapping(source = "filePath", target = "path")
    ResourcesResponse toDTO(Resource resource);
}

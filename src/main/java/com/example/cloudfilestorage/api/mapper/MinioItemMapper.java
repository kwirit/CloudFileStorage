package com.example.cloudfilestorage.api.mapper;

import com.example.cloudfilestorage.api.dto.ResourcesResponse;
import com.example.cloudfilestorage.core.utilities.ResourcesType;
import com.example.cloudfilestorage.core.utilities.PathUtilsService;
import io.minio.messages.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class MinioItemMapper {

    @Mapping(target = "name", expression = "java(this.getResourceName(minioItem))")
    @Mapping(target = "path", expression = "java(this.getPath(minioItem))")
    @Mapping(target = "size", expression = "java(this.getSize(minioItem))")
    @Mapping(target = "type", expression = "java(this.getResourceType(minioItem))")
    public abstract ResourcesResponse toDTO(Item minioItem);

    protected String getResourceName(Item minioItem) {
        return PathUtilsService.getResourceName(minioItem.objectName());
    }

    protected String getPath(Item minioItem) {
        return minioItem.objectName();
    }

    protected ResourcesType getResourceType(Item minioItem) {
        if (minioItem.isDir()) {
            return ResourcesType.DIRECTORY;
        } else {
            return ResourcesType.FILE;
        }
    }

    protected long getSize(Item minioItem) {
        if (getResourceType(minioItem).equals(ResourcesType.DIRECTORY)) {
            return 0;
        }
        else {
            return minioItem.size();
        }
    }
}
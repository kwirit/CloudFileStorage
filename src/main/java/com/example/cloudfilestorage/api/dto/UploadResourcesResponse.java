package com.example.cloudfilestorage.api.dto;


import com.example.cloudfilestorage.core.utilities.ResourcesType;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class UploadResourcesResponse {
    private String path;
    private String name;
    private long size;
    private ResourcesType type;

    public UploadResourcesResponse(String path, String name, long size, ResourcesType type) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.type = type;
    }
}

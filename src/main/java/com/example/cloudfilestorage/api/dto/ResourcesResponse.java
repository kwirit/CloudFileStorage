package com.example.cloudfilestorage.api.dto;


import com.example.cloudfilestorage.core.utilities.ResourcesType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RequiredArgsConstructor
@AllArgsConstructor
public class ResourcesResponse {
    private String path;
    private String name;
    private long size;
    private ResourcesType type;
}

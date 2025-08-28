package com.example.cloudfilestorage.core.service;

import com.example.cloudfilestorage.api.dto.ResourcesResponse;
import com.example.cloudfilestorage.api.mapper.ResourceMapper;
import com.example.cloudfilestorage.core.exception.ResourceException.FailedResourceOperationsException;
import com.example.cloudfilestorage.core.model.Resource;
import com.example.cloudfilestorage.core.model.User;
import com.example.cloudfilestorage.core.repository.ResourceRepository;
import com.example.cloudfilestorage.core.utilities.ResourcesType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ResourcePersistenceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;

    @Transactional
    public ResourcesResponse updateFolderInfo(String resourcesName, String path, User user) {
        Resource newResource = createNewResource(resourcesName, path, user, 0);

        resourceRepository.save(newResource);

        return resourceMapper.toDTO(newResource);
    }

    @Transactional
    public ResourcesResponse updateFileInfo(String resourcesName, String path, User user, long size) {
        Resource newResource = createNewResource(resourcesName, path, user, size);

        resourceRepository.save(newResource);

        return resourceMapper.toDTO(newResource);
    }
    @Transactional
    public List<ResourcesResponse> updateResourcesInfo(String objectName, MultipartFile file, User user) {
        List<String> resources = new ArrayList<>(Arrays.asList(objectName.split("/")));
        Map<String, Resource> existResources = resourceRepository.findAllByFilePathIn(resources)
                .stream()
                .collect(Collectors.toMap(Resource::getFilePath, resource -> resource));

        StringBuilder absolutePaths = new StringBuilder(resources.get(0));

        List<ResourcesResponse> uploadInfo = new ArrayList<>();

        for (String resource : resources) {
            absolutePaths.append("/").append(resource);
            String absolutePath = absolutePaths.toString();

            if (!existResources.containsKey(absolutePath)) {
                Resource newResource = createNewResource(
                        resource,
                        absolutePath,
                        user,
                        file.getSize()
                );
                existResources.put(absolutePath, newResource);

                uploadInfo.add(resourceMapper.toDTO(newResource));
            }
        }

        resourceRepository.saveAll(existResources.values());

        return uploadInfo;
    }

    @Transactional
    public Resource createNewResource(String resourcesName, String absolutePath, User ownerId, long size) {
        return Resource.builder()
                .fileName(resourcesName)
                .filePath(absolutePath)
                .ownerId(ownerId)
                .size(resourcesName.contains(".") ? size : 0)
                .type(resourcesName.contains(".") ? ResourcesType.FILE : ResourcesType.DIRECTORY)
                .build();
    }

    @Transactional
    public void deleteResource(String absolutePath) {
        Optional<Resource> resource = resourceRepository.findResourceByFilePath(absolutePath);

        resource.ifPresent(resourceRepository::delete);
    }

    @Transactional
    public ResourcesResponse getResourceInfo(String absolutePath) {
        Optional<Resource> resource = resourceRepository.findResourceByFilePath(absolutePath);
        if (resource.isPresent()) {
            return resourceMapper.toDTO(resource.get());
        }
        throw new FailedResourceOperationsException("Ошибка операции базы данных над ресурсом");
    }
}

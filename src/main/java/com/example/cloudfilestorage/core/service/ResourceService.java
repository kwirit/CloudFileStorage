package com.example.cloudfilestorage.core.service;

import com.example.cloudfilestorage.api.dto.UploadResourcesResponse;
import com.example.cloudfilestorage.api.mapper.ResourceMapper;
import com.example.cloudfilestorage.core.exception.ResourceException.UnauthorizedUserException;
import com.example.cloudfilestorage.core.model.Resource;
import com.example.cloudfilestorage.core.model.User;
import com.example.cloudfilestorage.core.repository.ResourceRepository;
import com.example.cloudfilestorage.core.utilities.ResourcesType;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final MinioClient minioClient;
    private final ResourceMapper resourceMapper;


    public List<UploadResourcesResponse> loadFile(String path, MultipartFile file) throws RuntimeException, Exception {
        User authenticatedUser = getAuthenticatedUserId();

        String objectName = buildPath(
                String.format("user-%d-files", authenticatedUser.getId()), path, file.getOriginalFilename()
        );

        isFileExist(objectName);

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("user-files")
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .build()
        );

        return updateResourcesInfo(objectName, file, authenticatedUser);
    }

    private void isFileExist(String objectName) throws Exception {
        minioClient.statObject(
                StatObjectArgs.builder().bucket("user-files")
                .object(objectName)
                .build()
        );
    }


    private static String buildPath(String... parts) {
        return String.join("/", parts);
    }

    private static User getAuthenticatedUserId() throws RuntimeException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return (User) auth.getPrincipal();
        }
        throw new UnauthorizedUserException("Необходима авторизация пользователя");
    }

    private List<UploadResourcesResponse> updateResourcesInfo(
            String objectName, MultipartFile file, User authenticatedUser
    ) {
        List<String> resources = new ArrayList<>(Arrays.asList(objectName.split("/")));
        Map<String, Resource> existResources =
                (resourceRepository.findAllByFilePathIn(resources))
                        .stream()
                        .collect(Collectors.toMap(Resource::getFilePath, resource -> resource));

        List<String> absoluteResourcesPaths = getAbsoluteResourcesPaths(resources);

        List<UploadResourcesResponse> uploadInfo = new ArrayList<>();

        for (int i = 0; i < resources.size(); i++) {
            String absolutePath = absoluteResourcesPaths.get(i);

            if (!existResources.containsKey(absolutePath)) {
                Resource newResource = createNewResources(
                        resources.get(i),
                        absolutePath,
                        authenticatedUser,
                        file.getSize()
                );
                existResources.put(absolutePath, newResource);

                uploadInfo.add(resourceMapper.toDTO(newResource));
            }
            else {
                updateResources(existResources.get(absolutePath), file.getSize());
            }
        }

        resourceRepository.saveAll(existResources.values());

        return uploadInfo;
    }

    private static List<String> getAbsoluteResourcesPaths(List<String> resources) {
        List<String> absoluteResourcesPaths = new ArrayList<>(resources.size());

        StringBuilder absolutePath = new StringBuilder(resources.get(0));

        for (int i = 0; i < resources.size(); i++) {
            absolutePath.append("/").append(resources.get(i));
            absoluteResourcesPaths.add(absolutePath.toString());
        }

        return absoluteResourcesPaths;
    }

    private void updateResources(Resource resource, long fileSize) {
        resource.setSize(fileSize);
    }


    private Resource createNewResources(
            String resourcesName,
            String absolutePath,
            User ownerId,
            long size
    ) {
        return Resource.builder()
                .fileName(resourcesName)
                .filePath(absolutePath)
                .ownerId(ownerId)
                .size(size)
                .type(resourcesName.contains(".") ? ResourcesType.FILE : ResourcesType.DIRECTORY)
                .build();
    }
}

package com.example.cloudfilestorage.core.service;

import com.example.cloudfilestorage.api.dto.UploadResourcesResponse;
import com.example.cloudfilestorage.api.mapper.ResourceMapper;
import com.example.cloudfilestorage.core.exception.ResourceException.FailedResourceLoadingException;
import com.example.cloudfilestorage.core.exception.ResourceException.FileAlreadyExistException;
import com.example.cloudfilestorage.core.exception.ResourceException.FolderDoesNotExistException;
import com.example.cloudfilestorage.core.model.Resource;
import com.example.cloudfilestorage.core.model.User;
import com.example.cloudfilestorage.core.repository.ResourceRepository;
import com.example.cloudfilestorage.core.utilities.ResourcesType;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.cloudfilestorage.core.utilities.Utilities.*;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final MinioClient minioClient;
    private final ResourceMapper resourceMapper;

    @Value("${minio.buckets.user-files-bucket}")
    private String userFilesBucketName;

    @Value("${minio.buckets.user-folder-pattern}")
    private static String userFolderPattern;


    @Transactional
    public List<UploadResourcesResponse> fileUploadProcessing(String path, MultipartFile[] file, User user) throws FailedResourceLoadingException {
        List<UploadResourcesResponse> responses = new ArrayList<>();
        for (MultipartFile multipartFile : file) {

            if (multipartFile.getOriginalFilename() == null || multipartFile.getOriginalFilename().isBlank()) {
                continue;
            }

            String objectName = buildPath(
                    getUserFolder(user), path, multipartFile.getOriginalFilename()
            );

            checkResourceAbsence(objectName);

            loadFileInStorage(multipartFile, objectName);

            responses.addAll(updateResourcesInfo(objectName, multipartFile, user));
        }

        return responses;
    }

    public void isFolderExists(String folderPath) {
        if (!folderPath.endsWith("/")) {
            folderPath += "/";
        }

        try {
            minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(userFilesBucketName)
                            .prefix(folderPath)
                            .delimiter("/")
                            .maxKeys(1)
                            .build());
        } catch (Exception e) {
            throw new FolderDoesNotExistException(e.getMessage());
        }
    }

    @Transactional
    public UploadResourcesResponse createNewFolderProcessing(String path, User user) throws FolderDoesNotExistException {
        String parentPath = getParentPath(path);
        String resourceName = getResourceName(path);

        String absolutePath = buildPath(userFilesBucketName, parentPath, resourceName);

        isFolderExists(parentPath);

        createFolderInStorage(absolutePath);

        return updateResourceInfo(resourceName, absolutePath, user);
    }

    private UploadResourcesResponse updateResourceInfo(String resourcesName,
                                                            String path,
                                                            User user) throws FailedResourceLoadingException {
        Resource newResource = createNewResource(resourcesName, path, user, 0);
        resourceRepository.save(newResource);
        return resourceMapper.toDTO(newResource);
    }

    public void createFolderInStorage(String folderName) {
        if (!folderName.endsWith("/")) {
            folderName += "/";
        }

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(userFilesBucketName)
                            .object(folderName)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {
            throw new FailedResourceLoadingException(e.getMessage());
        }
    }

    private void loadFileInStorage(MultipartFile file, String objectName) throws FailedResourceLoadingException {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(userFilesBucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );
        } catch (Exception e) {
            throw new FailedResourceLoadingException(e.getMessage());
        }
    }

    private static String getUserFolder(User authenticatedUser) {
        return String.format(userFolderPattern, authenticatedUser.getId());
    }

    private void checkResourceAbsence(String objectName) throws FailedResourceLoadingException {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(userFilesBucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new FileAlreadyExistException(e.getMessage());
        }
    }

    private List<UploadResourcesResponse> updateResourcesInfo(String objectName, MultipartFile file, User user) {
        List<String> resources = new ArrayList<>(Arrays.asList(objectName.split("/")));
        Map<String, Resource> existResources = resourceRepository.findAllByFilePathIn(resources)
                .stream()
                .collect(Collectors.toMap(Resource::getFilePath, resource -> resource));

        StringBuilder absolutePaths = new StringBuilder(resources.get(0));

        List<UploadResourcesResponse> uploadInfo = new ArrayList<>();

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

    private Resource createNewResource(String resourcesName, String absolutePath, User ownerId, long size) {
        return Resource.builder()
                .fileName(resourcesName)
                .filePath(absolutePath)
                .ownerId(ownerId)
                .size(resourcesName.contains(".") ? size : 0)
                .type(resourcesName.contains(".") ? ResourcesType.FILE : ResourcesType.DIRECTORY)
                .build();
    }

}

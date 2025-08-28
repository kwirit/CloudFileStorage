package com.example.cloudfilestorage.core.service;

import com.example.cloudfilestorage.api.dto.ResourcesResponse;
import com.example.cloudfilestorage.core.exception.ResourceException.FailedResourceOperationsException;
import com.example.cloudfilestorage.core.exception.ResourceException.FileAlreadyExistException;
import com.example.cloudfilestorage.core.exception.ResourceException.FileDoesNotExistException;
import com.example.cloudfilestorage.core.exception.ResourceException.FolderDoesNotExistException;
import com.example.cloudfilestorage.core.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

import static com.example.cloudfilestorage.core.utilities.PathUtilsService.*;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourcePersistenceService resourcePersistenceService;
    private final MinioService minioService;


    @Value("${minio.buckets.user-files-bucket}")
    private String userFilesBucketName;



    public InputStream downloadFileProcessing(String path, User user) {
        String absolutePath = getAbsolutPath(path, user);

        minioService.isFileExist(absolutePath);
        return minioService.getFile(absolutePath);
    }

    public InputStream downloadFolderAsZipProcessing(String path, User user) {
        String absolutePath = getAbsolutPath(path, user);

        minioService.isFolderExist(absolutePath);
        return minioService.getFolderAsZip(absolutePath);
    }


    public ResourcesResponse getResourcesInfoProcessing(String path, User user) throws FileDoesNotExistException, FolderDoesNotExistException{
        String absolutePath = getAbsolutPath(path, user);
        String resourceName = getResourceName(absolutePath);

        if (resourceName.contains(".")) {
            minioService.isFileExist(absolutePath);
        }
        else {
            minioService.isFolderExist(absolutePath);
        }
        return resourcePersistenceService.getResourceInfo(absolutePath);
    }

    public List<ResourcesResponse> getInfoAboutContentsDirectory(String path, User user) {
        String absolutePath = getAbsolutPath(path, user);

        minioService.isFolderExist(absolutePath);

        return minioService.getInfoAboutContentsDirectory(path);
    }

    @Transactional
    public List<ResourcesResponse> fileUploadProcessing(String path, MultipartFile[] file, User user) throws FailedResourceOperationsException, FileAlreadyExistException {
        List<ResourcesResponse> responses = new ArrayList<>();
        for (MultipartFile multipartFile : file) {

            if (multipartFile.getOriginalFilename() == null || multipartFile.getOriginalFilename().isBlank()) {
                continue;
            }

            String objectName = buildPath(
                    getUserFolder(user), path, multipartFile.getOriginalFilename()
            );


            try {
                if (minioService.isFileExist(objectName)) {
                    throw new FileAlreadyExistException();
                }
            } catch (FileDoesNotExistException ignored) {}

            minioService.loadFileInStorage(multipartFile, objectName);

            responses.addAll(resourcePersistenceService.updateResourcesInfo(objectName, multipartFile, user));
        }

        return responses;
    }

    @Transactional
    public ResourcesResponse createNewFolderProcessing(String path, User user) throws FolderDoesNotExistException {
        String absolutePath = getAbsolutPath(path, user);

        String parentPath = getParentPath(absolutePath);
        String resourceName = getResourceName(absolutePath);

        minioService.isFolderExist(parentPath);

        minioService.createFolderInStorage(absolutePath);

        return resourcePersistenceService.updateFolderInfo(resourceName, absolutePath, user);
    }

    @Transactional
    public void deleteResourceProcessing(String path, User user) throws FolderDoesNotExistException {
        String absolutePath = getAbsolutPath(path, user);
        String resourceName = getResourceName(absolutePath);

        if (resourceName.contains(".")) {
            deleteFileProcessing(absolutePath);
        } else {
            deleteFolderProcessing(absolutePath);
        }

        minioService.isFolderExist(getParentPath(path));
    }

    @Transactional
    public void deleteFolderProcessing(String path) throws FolderDoesNotExistException {
        minioService.isFolderExist(path);

        minioService.deleteFolderInStorage(path);

        resourcePersistenceService.deleteResource(path);
    }

    @Transactional
    public void deleteFileProcessing(String path) throws FileDoesNotExistException {
        minioService.isFileExist(path);

        minioService.deleteFileInStorage(path);

        resourcePersistenceService.deleteResource(path);
    }

    public ResourcesResponse moveAndRenameResourceProcessing(String from, String to, User user) {
        return (isRename(from, to))
                ? renameResourceProcessing(from, to, user)
                : moveResourceProcessing(from, to, user);
    }

    public ResourcesResponse renameResourceProcessing(String from, String to, User user) throws FileDoesNotExistException, FolderDoesNotExistException {
        String absolutePath = getAbsolutPath(from, user);
        String resourceName = getResourceName(absolutePath);
        if (resourceName.contains(".")) {
            return renameFileProcessing(absolutePath, to, user);
        }
        else {
            return moveFolderProcessing(absolutePath, to, user);
        }
    }

    public ResourcesResponse moveResourceProcessing(String from, String to, User user) throws FileDoesNotExistException {
        String absolutePath = getAbsolutPath(from, user);
        String resourceName = getResourceName(absolutePath);
        if (resourceName.contains(".")) {
            return renameFileProcessing(absolutePath, to, user);
        }
        else {
            return moveFolderProcessing(absolutePath, to, user);
        }
    }


    public ResourcesResponse renameFileProcessing(String absolutePathFrom, String to, User user)
            throws FileDoesNotExistException{
        String absolutePathTo = getAbsolutPath(to, user);

        minioService.isFileExist(absolutePathFrom);

        minioService.copyFile(absolutePathFrom, absolutePathTo);

        minioService.deleteFileInStorage(absolutePathFrom);

        resourcePersistenceService.deleteResource(absolutePathFrom);
        return resourcePersistenceService.updateFileInfo(
                getResourceName(absolutePathTo),
                absolutePathTo,
                user,
                minioService.getFileSize(absolutePathFrom));
    }

    public ResourcesResponse moveFolderProcessing(String absolutePathFrom, String to, User user)
            throws FolderDoesNotExistException{
        String absolutePathTo = getAbsolutPath(to, user);

        minioService.isFolderExist(absolutePathFrom);

        minioService.copyFolder(absolutePathFrom, absolutePathTo);

        minioService.deleteFolderInStorage(absolutePathFrom);

        resourcePersistenceService.deleteResource(absolutePathFrom);

        return resourcePersistenceService.updateFolderInfo(
                getResourceName(absolutePathTo),
                absolutePathTo,
                user
        );
    }
}

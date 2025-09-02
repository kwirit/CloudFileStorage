package com.example.cloudfilestorage.core.service;

import com.example.cloudfilestorage.api.dto.ResourcesResponse;
import com.example.cloudfilestorage.core.exception.ResourceException.FailedResourceOperationsException;
import com.example.cloudfilestorage.core.exception.ResourceException.FileAlreadyExistException;
import com.example.cloudfilestorage.core.exception.ResourceException.FileDoesNotExistException;
import com.example.cloudfilestorage.core.exception.ResourceException.FolderDoesNotExistException;
import com.example.cloudfilestorage.core.model.User;
import com.example.cloudfilestorage.core.repository.ResourceRepository;
import com.example.cloudfilestorage.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

import static com.example.cloudfilestorage.core.utilities.PathUtilsService.*;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final UserService userService;
    private final ResourcePersistenceService resourcePersistenceService;
    private final MinioService minioService;


    public InputStream downloadFileProcessing(String path) {
        User user = userService.getCurrentUser();
        String absolutePath = getAbsolutPath(path, user);

        if (!minioService.isFileExist(absolutePath)) {
            throw new FileDoesNotExistException(String.format("Файл по пути: %s не найден", path));
        }
        return minioService.getFile(absolutePath);
    }

    public InputStream downloadFolderAsZipProcessing(String path) {
        User user = userService.getCurrentUser();
        String absolutePath = getAbsolutPath(path, user);

        if (!minioService.isFolderExist(absolutePath)) {
            throw new FolderDoesNotExistException(String.format("Папка по пути: %s не найдена", path));
        }
        return minioService.getFolderAsZip(absolutePath);
    }


    public ResourcesResponse getResourcesInfoProcessing(String path) throws FileDoesNotExistException, FolderDoesNotExistException{
        User user = userService.getCurrentUser();
        String absolutePath = getAbsolutPath(path, user);
        String resourceName = getResourceName(absolutePath);

        if (resourceName.contains(".")) {
            if (!minioService.isFileExist(absolutePath)) {
                throw new FileDoesNotExistException(String.format("Файл по пути: %s не найден", path));
            }
        }
        else {
            if (!minioService.isFolderExist(absolutePath)) {
                throw new FolderDoesNotExistException(String.format("Папка по пути: %s не найдена", path));
            }
        }
        return resourcePersistenceService.getResourceInfo(absolutePath);
    }

    public List<ResourcesResponse> getInfoAboutContentsDirectory(String path) {
        User user = userService.getCurrentUser();
        String absolutePath = getAbsolutPath(path, user);

        if (!minioService.isFolderExist(absolutePath)) {
            throw new FolderDoesNotExistException(String.format("Папка по пути: %s не найдена", path));
        }

        return minioService.getInfoAboutContentsDirectory(absolutePath);
    }

    public List<ResourcesResponse> fileUploadProcessing(String path, MultipartFile[] file) throws FailedResourceOperationsException, FileAlreadyExistException {
        User user = userService.getCurrentUser();
        List<ResourcesResponse> responses = new ArrayList<>();
        for (MultipartFile multipartFile : file) {

            if (multipartFile.getOriginalFilename() == null || multipartFile.getOriginalFilename().isBlank()) {
                continue;
            }

            String objectName = buildPath(
                    getUserFolder(user), path, multipartFile.getOriginalFilename()
            );


            if (minioService.isFileExist(objectName)) {
                throw new FileAlreadyExistException();
            }

            minioService.loadFileInStorage(multipartFile, objectName);

            responses.addAll(resourcePersistenceService.updateResourcesInfo(objectName, multipartFile, user));
        }

        return responses;
    }

    public ResourcesResponse createNewFolderProcessing(String path) throws FolderDoesNotExistException {
        User user = userService.getCurrentUser();
        String absolutePath = getAbsolutPath(path, user);

        String parentPath = getParentPath(absolutePath);
        String resourceName = getResourceName(absolutePath);

        if (!minioService.isFolderExist(parentPath)) {
            throw new FolderDoesNotExistException(String.format("Папка по пути: %s не найдена", path));
        }

        minioService.createFolderInStorage(absolutePath);

        return resourcePersistenceService.updateFolderInfo(resourceName, absolutePath, user);
    }

    public void deleteResourceProcessing(String path) throws FolderDoesNotExistException {
        User user = userService.getCurrentUser();
        String absolutePath = getAbsolutPath(path, user);
        String resourceName = getResourceName(absolutePath);

        if (resourceName.contains(".")) {
            deleteFileProcessing(absolutePath);
        } else {
            deleteFolderProcessing(absolutePath);
        }

        minioService.isFolderExist(getParentPath(absolutePath));
    }

    public void deleteFolderProcessing(String path) throws FolderDoesNotExistException {
        if (!minioService.isFolderExist(path)) {
            throw new FolderDoesNotExistException(String.format("Папка по пути: %s не найдена", path));
        }

        minioService.deleteFolderInStorage(path);

        resourcePersistenceService.deleteResource(path);
    }

    public void deleteFileProcessing(String path) throws FileDoesNotExistException {
        if (!minioService.isFileExist(path)) {
            throw new FileDoesNotExistException(String.format("Файл по пути: %s не найден", path));
        }

        minioService.deleteFileInStorage(path);

        resourcePersistenceService.deleteResource(path);
    }

    public ResourcesResponse moveAndRenameResourceProcessing(String from, String to) {
        User user = userService.getCurrentUser();
        return moveResourceProcessing(from, to, user);
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

        if (!minioService.isFileExist(absolutePathFrom)) {
            throw new FileDoesNotExistException("Файл источника не существует");
        };

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

        if (!minioService.isFolderExist(absolutePathFrom)){
            throw new FolderDoesNotExistException(String.format("Папка по пути: %s не найден", absolutePathFrom));
        }

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

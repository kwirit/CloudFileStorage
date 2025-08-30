package com.example.cloudfilestorage.core.service;


import com.example.cloudfilestorage.api.dto.ResourcesResponse;
import com.example.cloudfilestorage.api.mapper.MinioItemMapper;
import com.example.cloudfilestorage.core.exception.ResourceException.FailedResourceOperationsException;
import com.example.cloudfilestorage.core.exception.ResourceException.FileDoesNotExistException;
import com.example.cloudfilestorage.core.exception.ResourceException.FolderDoesNotExistException;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioItemMapper minioItemMapper;

    @Value("${minio.buckets.user-files-bucket}")
    private String userFilesBucketName;

    public boolean isFileExist(String objectName) throws FileDoesNotExistException {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(userFilesBucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            throw new FileDoesNotExistException(e.getMessage());
        }
    }

    public boolean isFolderExist(String folderPath) throws FolderDoesNotExistException {
        if (!folderPath.endsWith("/")) {
            folderPath += "/";
        }

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(userFilesBucketName)
                            .prefix(folderPath)
                            .delimiter("/")
                            .maxKeys(1)
                            .build());
            List<DeleteObject> objectsToDelete = StreamSupport.stream(results.spliterator(), false)
                    .map(itemResult -> {
                        try {
                            return new DeleteObject(itemResult.get().objectName());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (!objectsToDelete.isEmpty()) {
                return true;
            }
            throw new FolderDoesNotExistException(folderPath);
        } catch (FolderDoesNotExistException e) {
            throw e;
        } catch (Exception e) {
            throw new FailedResourceOperationsException(e.getMessage());
        }
    }

    public void loadFileInStorage(MultipartFile file, String objectName) throws FailedResourceOperationsException {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(userFilesBucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );
        } catch (Exception e) {
            throw new FailedResourceOperationsException(e.getMessage());
        }
    }

    public void createFolderInStorage(String folderName) throws FailedResourceOperationsException {
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
            throw new FailedResourceOperationsException(e.getMessage());
        }
    }

    public void deleteFileInStorage(String path) throws FailedResourceOperationsException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(userFilesBucketName)
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            throw new FailedResourceOperationsException(e.getMessage());
        }
    }

    public void deleteFolderInStorage(String path) throws FailedResourceOperationsException {

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(userFilesBucketName)
                            .prefix(path)
                            .recursive(true)
                            .build()
            );

            List<DeleteObject> objectsToDelete = StreamSupport.stream(results.spliterator(), false)
                    .map(itemResult -> {
                        try {
                            return new DeleteObject(itemResult.get().objectName());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());


            if (!objectsToDelete.isEmpty()) {
                Iterable<Result<DeleteError>> errorResults = minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket(userFilesBucketName)
                                .objects(objectsToDelete)
                                .build()
                );
                boolean hasErrors = false;

                StringBuilder errors = new StringBuilder();

                for (Result<DeleteError> errorResult : errorResults) {
                    DeleteError error = errorResult.get();
                    errors.append(
                            String.format("Ошибка при удалении объекта '%s'; %s", error.objectName(), error.message())
                    );
                    hasErrors = true;
                }

                if (hasErrors) {
                    throw new FailedResourceOperationsException("Не удалось удалить один или несколько объектов." + errors);
                }
            }

        } catch (Exception e) {
            throw new FailedResourceOperationsException(e.getMessage());
        }
    }

    public InputStream getFile(String path) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(userFilesBucketName)
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            throw new FailedResourceOperationsException(e.getMessage());
        }
    }

    public InputStream getFolderAsZip(String folderPath) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(userFilesBucketName)
                            .prefix(folderPath)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                try (InputStream is = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(userFilesBucketName)
                                .object(objectName)
                                .build())
                ) {
                    String relativePath = objectName.substring(folderPath.length());
                    ZipEntry entry = new ZipEntry(relativePath);
                    zos.putNextEntry(entry);

                    is.transferTo(zos);
                }
                zos.closeEntry();
            }
        } catch (Exception e) {
            throw new FailedResourceOperationsException(e.getMessage());
        }
        return new ByteArrayInputStream(bos.toByteArray());
    }

    public List<ResourcesResponse> getInfoAboutContentsDirectory(String path) throws FailedResourceOperationsException {
        if (!path.endsWith("/")) {
            path += "/";
        }
        List<ResourcesResponse> responses = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(userFilesBucketName)
                            .prefix(path)
                            .delimiter("/")
                            .recursive(false)
                            .build());
            for (Result<Item> result : results) {
                responses.add(minioItemMapper.toDTO(result.get()));
            }
            return responses;
        } catch (Exception e) {
            throw new FailedResourceOperationsException(e.getMessage());
        }
    }

    public void copyFile(String from, String to) throws FailedResourceOperationsException {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(userFilesBucketName)
                            .source(
                                    CopySource.builder()
                                            .bucket(userFilesBucketName)
                                            .object(from)
                                            .build()
                            )
                            .object(to)
                            .build()
            );
        } catch (Exception e) {
            throw new FailedResourceOperationsException(e.getMessage());
        }
    }

    public void copyFolder(String from, String to) throws FailedResourceOperationsException {
        if (!from.endsWith("/")) {
            from += "/";
        }

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(userFilesBucketName)
                            .prefix(from)
                            .recursive(true)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                String destObjectName = to + objectName.substring(from.length());

                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(userFilesBucketName)
                                .source(
                                        CopySource.builder()
                                                .bucket(userFilesBucketName)
                                                .object(objectName)
                                                .build()
                                )
                                .object(destObjectName)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new FailedResourceOperationsException(e.getMessage());
        }
    }

    public long getFileSize(String path) throws FailedResourceOperationsException {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(userFilesBucketName)
                            .object(path)
                            .build()
            );
            return stat.size();
        } catch (Exception e) {
            throw new FailedResourceOperationsException(e.getMessage());
        }
    }


}

package com.example.cloudfilestorage.service;

import com.example.cloudfilestorage.api.dto.ResourcesResponse;
import com.example.cloudfilestorage.core.exception.ResourceException.FailedResourceOperationsException;
import com.example.cloudfilestorage.core.exception.ResourceException.FileDoesNotExistException;
import com.example.cloudfilestorage.core.exception.ResourceException.FolderDoesNotExistException;
import com.example.cloudfilestorage.core.service.MinioService;
import com.example.cloudfilestorage.core.utilities.ResourcesType;
import io.minio.*;
import io.minio.messages.Item;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.minio.MakeBucketArgs.builder;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;

@SpringBootTest
@Testcontainers
class MinioServiceTest {

    @Container
    private static final MinIOContainer MINIO_CONTAINER = new MinIOContainer("minio/minio:latest")
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin");

    @Autowired
    private MinioService minioService;

    @Autowired
    private MinioClient minioClient;

    private static final String BUCKET = "user-files";

    @DynamicPropertySource
    static void setMinioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.endpoint", MINIO_CONTAINER::getS3URL);
        registry.add("minio.accessKey", MINIO_CONTAINER::getUserName);
        registry.add("minio.secretKey", MINIO_CONTAINER::getPassword);
        registry.add("minio.buckets.user-files-bucket", () -> BUCKET);
    }

    @BeforeEach
    void setupBucketAndCleanupPreviousTest() throws Exception {
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(BUCKET).build()
        );
        if (!found) {
            minioClient.makeBucket(builder().bucket(BUCKET).build());
        }

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(BUCKET)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> item : results) {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(item.get().objectName())
                            .build()
            );
        }
    }

    @Test
    void createEmptyFolderTest() throws Exception {
        String folderName = "kwiritFolder/folder1/folder2";

        minioService.createFolderInStorage(folderName);

        assertTrue(
                minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(BUCKET)
                                .prefix(folderName)
                                .delimiter("/")
                                .maxKeys(1)
                                .build()
                ).iterator().hasNext()
        );
    }

    @Test
    void notExistFolderTestToException() throws Exception {
        String notExistFolderName = "kwirit/folder/notExistFolder";

        assertThrows(FolderDoesNotExistException.class, () -> {
            minioService.isFolderExist(notExistFolderName);
        });
    }

    @Test
    void existFolderTest() throws Exception {
        String folderName = "kwiritFolder/folder1";

        minioService.createFolderInStorage(folderName);

        assertTrue(minioService.isFolderExist(folderName));
    }


    @Test
    void loadFileTest() throws Exception {
        String content = "This is a test file content.";
        String fileName = "test-file.txt";

        MultipartFile file = createTextMockFile(fileName, content);

        assertDoesNotThrow(() -> {
            minioService.loadFileInStorage(file, fileName);
        });
    }

    @Test
    void notExistFileTestToException() throws Exception {
        String notExistFileName = "Gleb228.txt";

        assertThrows(FileDoesNotExistException.class, () -> {
            minioService.isFileExist(notExistFileName);
        });
    }

    @Test
    void existFileTest() throws Exception {
        String content = "Privet menya zovut Gleb.";
        String fileName = "test-file.txt";


        MultipartFile file = createTextMockFile(fileName, content);

        minioService.loadFileInStorage(file, fileName);

        assertTrue(minioService.isFileExist(fileName));
    }

    @Test
    void deleteFileTest() throws Exception {
        String content = "Do do do sogli sogli.";
        String fileName = "test-file.txt";

        MultipartFile file = createTextMockFile(fileName, content);

        putInStorage(file, fileName);

        minioService.deleteFileInStorage(fileName);

        assertThrows(FileDoesNotExistException.class, () ->
                {
                    minioService.isFileExist(fileName);
                }
        );
    }

    @Test
    void deleteFolderTest() throws Exception {
        String content = "Do do do sogli sogli.";
        String fileName = "test-file.txt";
        String path = "kwirit/folder1/folder2";
        String absolutePath = path + "/" + fileName;

        MultipartFile file = createTextMockFile(fileName, content);
        putInStorage(file, absolutePath);

        minioService.deleteFolderInStorage(path);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThrows(FolderDoesNotExistException.class, () -> {
                    minioService.isFolderExist(path);
                })
        );
    }

    @Test
    void getFileTest() throws Exception {
        String content = "Do do do sogli sogli.";
        String fileName = "file.txt";
        String path = "kwirit/folder1/folder2";
        String absolutePath = path + "/" + fileName;

        MultipartFile file = createTextMockFile(fileName, content);
        putInStorage(file, absolutePath);

        InputStream inputStream = minioService.getFile(absolutePath);

        assertEquals(content, new String(inputStream.readAllBytes()));
    }

    @Test
    void getFileTestToException() throws Exception {
        String folderName = "kwirit/folder0123/folder1/hehe.jpeg";

        assertThrows(FailedResourceOperationsException.class, () -> {
            minioService.getFile(folderName);
        });
    }


    @Test
    void getInfoAboutContentsDirectoryTest() throws Exception {
        String content = "Do do do sogli sogli.";
        String fileName = "file.txt";
        String path = "kwirit/folder1/folder2";
        String absolutePath = path + "/" + fileName;

        MultipartFile file = createTextMockFile(fileName, content);
        putInStorage(file, absolutePath);

        minioService.createFolderInStorage(path + "/folder3/");

        List<ResourcesResponse> result = minioService.getInfoAboutContentsDirectory(path);

        List<ResourcesResponse> expected = new ArrayList<>(List.of(
                new ResourcesResponse(absolutePath, fileName, file.getSize(), ResourcesType.FILE),
                new ResourcesResponse(path + "/folder3/", "folder3", 0, ResourcesType.DIRECTORY)
        ));

        assertEquals(expected, result);
    }

    @Test
    void copyFileTest() throws Exception {
        String content = "Do do do sogli sogli.";
        String fileName = "file.txt";
        String path = "kwirit/folder1/folder2";
        String absolutePath = path + "/" + fileName;

        String absolutePath2 = "kenopsia/folder3/file.txt";

        MultipartFile file = createTextMockFile(fileName, content);

        putInStorage(file, absolutePath);

        minioService.copyFile(absolutePath, absolutePath2);
        minioService.deleteFileInStorage(absolutePath);

        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
                assertAll("Проверка копирования",
                        () -> assertThrows(FileDoesNotExistException.class, () -> {
                            minioService.isFileExist(absolutePath);
                        }),
                        () -> assertTrue(minioService.isFileExist(absolutePath2))
                )
        );
    }

    @Test
    void copyFolderTest() throws Exception {
        String folderPath1 = "kwirit/folder1/";
        String folderPath2 = "kenopsia/folder2/";
        MultipartFile file1 = createTextMockFile("hehe.txt", "hehe-hehe hehe-hehe");
        MultipartFile file2 = createTextMockFile("not hehe.txt", "not hehe-hehe not hehe-hehe");

        putInStorage(file1, folderPath1);
        putInStorage(file2, folderPath1);

        minioService.copyFolder(folderPath1, folderPath2);
        minioService.deleteFolderInStorage(folderPath1);

        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
                assertAll("Проверка копирования",
                        () -> assertThrows(FolderDoesNotExistException.class, () -> {
                            minioService.isFolderExist(folderPath1);
                        }),
                        () -> assertTrue(minioService.isFolderExist(folderPath2))
                )
        );
    }

    @Test
    void getFileSizeTest() throws Exception {
        String content = "Bombordiro-crocodilo.";
        String fileName = "file.txt";

        String path = "kwirit/folder1/folder2";
        String absolutePath = path + "/" + fileName;

        MultipartFile file = createTextMockFile(fileName, content);
        putInStorage(file, absolutePath);

        assertEquals(minioService.getFileSize(absolutePath), file.getSize());
    }

    public MultipartFile createTextMockFile(String fileName, String content) throws Exception {
        return new MockMultipartFile(
                "file",
                fileName,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );

    }

    public void putInStorage(MultipartFile file, String path)
            throws Exception {

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(path)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .build()
        );
    }

}
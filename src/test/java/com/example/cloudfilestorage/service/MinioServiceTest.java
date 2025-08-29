package com.example.cloudfilestorage.service;

import com.example.cloudfilestorage.core.service.MinioService;
import io.minio.*;
import io.minio.messages.Item;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.minio.MakeBucketArgs.builder;

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
        // Проверяем и создаем бакет, если его нет
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(BUCKET).build()
        );
        if (!found) {
            minioClient.makeBucket(builder().bucket(BUCKET).build());
        }

        // Очищаем содержимое бакета перед каждым тестом
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
}
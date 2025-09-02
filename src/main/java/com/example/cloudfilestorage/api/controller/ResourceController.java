package com.example.cloudfilestorage.api.controller;

import com.example.cloudfilestorage.api.dto.ResourcesResponse;
import com.example.cloudfilestorage.core.model.User;
import com.example.cloudfilestorage.core.service.ResourceService;
import jakarta.validation.constraints.Pattern;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

import static com.example.cloudfilestorage.core.utilities.PathUtilsService.getResourceName;

@RestController
@RequestMapping("/api/resource")
@Validated
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/{path}")
    public ResponseEntity<?> getResourceInfo(
            @PathVariable String path
    ) {
        return ResponseEntity.ok(resourceService.getResourcesInfoProcessing(path));
    }

    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestParam("path") String path,
            @RequestParam("file") MultipartFile[] file
    ) {
        List<ResourcesResponse> resourcesResponse = resourceService.fileUploadProcessing(path, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourcesResponse);
    }

    @GetMapping("/download/{path}")
    public ResponseEntity<?> downloadResource(
            @Pattern(regexp = "([a-zA-Z_\\s.-]+/)*")
            @PathVariable String path
    ) {
        if (path.contains(".")) {
            InputStream fileStream = resourceService.downloadFileProcessing(path);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + getResourceName(path) + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(fileStream));
        } else {
            InputStream zipStream = resourceService.downloadFolderAsZipProcessing(path);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + getResourceName(path) + ".zip\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(zipStream));
        }
    }

    @GetMapping("/move/{from}{to}")
    public ResponseEntity<?> moveResource(
            @Pattern(regexp = "([a-zA-Z_\\s.-]*/)*([a-zA-Z_\\s-]*(.[a-zA-Z]*)?)")
            @PathVariable String from,
            @PathVariable String to
    ) {
        return ResponseEntity.ok(resourceService.moveAndRenameResourceProcessing(from, to));
    }
}

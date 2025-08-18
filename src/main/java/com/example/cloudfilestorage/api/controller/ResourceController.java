package com.example.cloudfilestorage.api.controller;

import com.example.cloudfilestorage.api.dto.UploadResourcesResponse;
import com.example.cloudfilestorage.core.service.ResourceService;
import io.minio.messages.Upload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.parser.Entity;
import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping("/resource{path}")
    public ResponseEntity<?> uploadFile(
            @Pattern(regexp= "([a-zA-Z_\\s.-]*/)*([a-zA-Z_\\s-]*(.[a-zA-Z]*)?)")
            @PathVariable String path,
            @RequestParam MultipartFile file) {
        try {
            List<UploadResourcesResponse> uploadResourcesResponse = resourceService.loadFile(path, file);
            return ResponseEntity
                    .ok()
                    .body(uploadResourcesResponse);
        } catch (ErrorResponseException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

package com.example.cloudfilestorage.api.controller;

import com.example.cloudfilestorage.api.dto.UploadResourcesResponse;
import com.example.cloudfilestorage.core.model.User;
import com.example.cloudfilestorage.core.service.ResourceService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping("/resource/{path}")
    public ResponseEntity<?> uploadFile(
            @Pattern(regexp= "([a-zA-Z_\\s.-]*/)*([a-zA-Z_\\s-]*(.[a-zA-Z]*)?)")
            @PathVariable String path,
            @RequestParam MultipartFile[] file,
            Authentication auth
    ) {
        List<UploadResourcesResponse> uploadResourcesResponse = resourceService.fileUploadProcessing(
                path, file, (User) auth.getPrincipal()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(uploadResourcesResponse);
    }

    @PostMapping("/directory/{path}")
    public ResponseEntity<?> createEmptyDirectory(
            @Pattern(regexp= "([a-zA-Z_\\s.-]*/)*([a-zA-Z_\\s-]*(.[a-zA-Z]*)?)")
            @PathVariable String path,
            Authentication auth
    ) {
        UploadResourcesResponse uploadResourcesResponse = resourceService.createNewFolderProcessing(
                path, (User) auth.getPrincipal()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(uploadResourcesResponse);
    }
}

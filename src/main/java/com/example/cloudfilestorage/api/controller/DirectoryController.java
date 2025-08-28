package com.example.cloudfilestorage.api.controller;

import com.example.cloudfilestorage.api.dto.ResourcesResponse;
import com.example.cloudfilestorage.core.model.User;
import com.example.cloudfilestorage.core.service.ResourceService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@Validated
@RequiredArgsConstructor
public class DirectoryController {

    private final ResourceService resourceService;

    @PostMapping("/{path}")
    public ResponseEntity<?> createEmptyDirectory(
            @Pattern(regexp= "([a-zA-Z_\\s.-]*/)*([a-zA-Z_\\s-]*(.[a-zA-Z]*)?)")
            @PathVariable String path,
            Authentication auth
    ) {
        ResourcesResponse resourcesResponse = resourceService.createNewFolderProcessing(
                path, (User) auth.getPrincipal()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourcesResponse);
    }

    @GetMapping("/{path}")
    public ResponseEntity<?> getInfoAboutContentsDirectory(
            @Pattern(regexp= "([a-zA-Z_\\s.-]*/)*([a-zA-Z_\\s-]*(.[a-zA-Z]*)?)")
            @PathVariable String path,
            Authentication auth
    ) {
        List<ResourcesResponse> resourcesResponse = resourceService.getInfoAboutContentsDirectory(path, (User) auth.getPrincipal());
        return ResponseEntity.
                ok(resourcesResponse);
    }

    @DeleteMapping("/{path}")
    public ResponseEntity<?> deleteResource(
            @Pattern(regexp= "([a-zA-Z_\\s.-]*/)*([a-zA-Z_\\s-]*(.[a-zA-Z]*)?)")
            @PathVariable String path,
            Authentication auth
    ) {
        resourceService.deleteResourceProcessing(path, (User) auth.getPrincipal());

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

}

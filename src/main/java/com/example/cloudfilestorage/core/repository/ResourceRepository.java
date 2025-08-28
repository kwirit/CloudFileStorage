package com.example.cloudfilestorage.core.repository;

import com.example.cloudfilestorage.core.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findAllByFilePathIn(List<String> resourcesNames);

    Optional<Resource> findResourceByFilePath(String filePath);
}

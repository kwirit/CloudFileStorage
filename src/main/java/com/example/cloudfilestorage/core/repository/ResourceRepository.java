package com.example.cloudfilestorage.core.repository;

import com.example.cloudfilestorage.core.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    public List<Resource> findAllByFilePathIn(List<String> resourcesNames);
}

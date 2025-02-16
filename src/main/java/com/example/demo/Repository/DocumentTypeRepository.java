package com.example.demo.Repository;

import com.example.demo.Model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {
    boolean existsByName(String name);
}

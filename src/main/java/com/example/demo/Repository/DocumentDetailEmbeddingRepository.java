package com.example.demo.Repository;

import com.example.demo.Model.DocumentDetailEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentDetailEmbeddingRepository extends JpaRepository<DocumentDetailEmbedding, Long> {
    List<DocumentDetailEmbedding> findByDocumentId(Long documentId);
}

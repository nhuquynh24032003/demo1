package com.example.demo.Repository;

import com.example.demo.Model.DocumentDetailEmbeddingES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentDetailEmbeddingESRepository extends ElasticsearchRepository<DocumentDetailEmbeddingES, String> {
    List<DocumentDetailEmbeddingES> findByDocumentId(Long documentId);
}

package com.example.demo.Model;

import jakarta.persistence.*;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Entity
@Table(name = "DocumentDetailEmbedding")
public class DocumentDetailEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "chunk_text", columnDefinition = "TEXT", nullable = false)
    private String chunkText;

    @Lob
    @Column(name = "embedding", columnDefinition = "TEXT")
    private String embedding; // Lưu dưới dạng chuỗi JSON

    // Constructor không tham số
    public DocumentDetailEmbedding() {}

    // Constructor đầy đủ
    public DocumentDetailEmbedding(Long id, Long documentId, int chunkIndex, String chunkText,String embedding) {
        this.id = id;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.chunkText = chunkText;
        this.embedding = embedding;
    }

    // Getter và Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getChunkText() {
        return chunkText;
    }

    public void setChunkText(String chunkText) {
        this.chunkText = chunkText;
    }

    public String getEmbedding() {
        return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }
}

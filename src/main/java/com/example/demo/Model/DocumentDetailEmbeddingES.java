package com.example.demo.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "document_embedding")
public class DocumentDetailEmbeddingES {
    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long documentId;

    @Field(type = FieldType.Integer)
    private int chunkIndex;

    @Field(type = FieldType.Text)
    private String chunkText;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Dense_Vector, dims = 768)
    private List<Double> embedding;

    @Field(type = FieldType.Dense_Vector, dims = 768)
    private List<Double> embedding_title;
    public DocumentDetailEmbeddingES() {}

    public DocumentDetailEmbeddingES(Long documentId, int chunkIndex, String chunkText, List<Double> embedding) {
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.chunkText = chunkText;
        this.embedding = embedding;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getChunkText() { return chunkText; }
    public void setChunkText(String chunkText) { this.chunkText = chunkText; }

    public List<Double> getEmbedding() { return embedding; }
    public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTitle() { return title; }
    public void setTitle(String content) { this.title = title; }

    public List<Double> getEmbedding_title() { return embedding_title; }
    public void setEmbedding_title(List<Double> embedding_title) { this.embedding_title = embedding_title; }
}

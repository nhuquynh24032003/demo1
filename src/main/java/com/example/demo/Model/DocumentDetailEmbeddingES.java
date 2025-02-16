package com.example.demo.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
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
    @Field(type = FieldType.Text)
    private String issuingAgency;
    @Field(type = FieldType.Text)// Cơ quan ban hành
    private String officialGazetteNumber;
    @Field(type = FieldType.Text)// Số công báo
    private String documentNumber;
    @Field(type = FieldType.Text)// Số hiệu
    private String publicationDate;
    @Field(type = FieldType.Text)// Ngày đăng công báo
    private String documentType;
    @Field(type = FieldType.Text)// Loại văn bản
    private String signer;
    @Field(type = FieldType.Text)// Người ký
    private LocalDate issuedDate;
    @Field(type = FieldType.Text)// Ngày ban hành
    private String effectiveDate;
    @Field(type = FieldType.Text)// Ngày hết hiệu lực
    private String fields;// Lĩnh vực

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
    public void setTitle(String title) { this.title = title; }

    public List<Double> getEmbedding_title() { return embedding_title; }
    public void setEmbedding_title(List<Double> embedding_title) { this.embedding_title = embedding_title; }
    public String getIssuingAgency() {
        return issuingAgency;
    }
    public void setIssuingAgency(String issuingAgency) {
        this.issuingAgency = issuingAgency;
    }
    public String getOfficialGazetteNumber() {
        return officialGazetteNumber;
    }
    public void setOfficialGazetteNumber(String officialGazetteNumber) {
        this.officialGazetteNumber = officialGazetteNumber;
    }
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
    public String getDocumentNumber() {
        return documentNumber;
    }
    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }
    public String getPublicationDate() {
        return publicationDate;
    }
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
    public String getDocumentType() {
        return documentType;
    }
    public void setSigner(String signer) {
        this.signer = signer;
    }
    public String getSigner() {
        return signer;
    }
    public void setIssuedDate(LocalDate issuedDate) {
        this.issuedDate = issuedDate;
    }
    public LocalDate getIssuedDate() {
        return issuedDate;
    }
    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
    public String getEffectiveDate() {
        return effectiveDate;
    }
    public void setFields(String fields) {
        this.fields = fields;
    }
    public String getFields() {
        return fields;
    }
}

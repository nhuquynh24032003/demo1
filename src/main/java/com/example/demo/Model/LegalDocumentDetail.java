package com.example.demo.Model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "legal_document_details")
public class LegalDocumentDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String detailUrl;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    private String issuingAgency; // Cơ quan ban hành
    private String officialGazetteNumber; // Số công báo
    private String documentNumber; // Số hiệu
    private String publicationDate; // Ngày đăng công báo
    private String documentType; // Loại văn bản
    private String signer; // Người ký
    private String title;
    private String issuedDate; // Ngày ban hành
    private String effectiveDate; // Ngày hết hiệu lực
    private String fields; // Lĩnh vực
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String pdfUrl;

    public LegalDocumentDetail(String detailUrl, String content,String issuingAgency, String officialGazetteNumber, String publicationDate, String documentType, String signer, String title, String issuedDate, String documentNumber, String pdfUrl, String fields) {
        this.content = content;
        this.detailUrl = detailUrl;
        this.issuingAgency = issuingAgency;
        this.officialGazetteNumber = officialGazetteNumber;
        this.publicationDate = publicationDate;
        this.documentType = documentType;
        this.signer = signer;
        this.title = title;
        this.issuedDate = issuedDate;
        this.documentNumber = documentNumber;
        this.pdfUrl = pdfUrl;
        this.fields = fields;
    }

    public LegalDocumentDetail() {

    }

    public String getPdfUrl() {
        return pdfUrl;
    }
    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

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
    public void setIssuedDate(String issuedDate) {
        this.issuedDate = issuedDate;
    }
    public String getIssuedDate() {
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
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

}

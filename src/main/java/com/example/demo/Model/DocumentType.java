package com.example.demo.Model;

import jakarta.persistence.*;
@Entity
@Table(name = "document_types")
public class DocumentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    public DocumentType(String name) {
        this.name = name;
    }
    public DocumentType() {}

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
}

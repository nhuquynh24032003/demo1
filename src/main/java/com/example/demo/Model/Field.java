package com.example.demo.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "fields")
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    public Field(String name) {
        this.name = name;
    }
    public Field() {}
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
}

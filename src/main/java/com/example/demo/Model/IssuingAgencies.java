package com.example.demo.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "issuing_agencies")
public class IssuingAgencies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    public IssuingAgencies(String name) {
        this.name = name;
    }

    public IssuingAgencies() {

    }


    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
}
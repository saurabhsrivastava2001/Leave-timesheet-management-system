package com.leavemanagement.leaveservice.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String description;

    public Holiday() {
    }

    public Holiday(Long id, LocalDate date, String description) {
        this.id = id;
        this.date = date;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

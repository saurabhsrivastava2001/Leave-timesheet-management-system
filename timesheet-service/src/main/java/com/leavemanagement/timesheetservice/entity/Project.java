package com.leavemanagement.timesheetservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String projectCode;

    @Column(nullable = false)
    private String name;

    private String description;
    
    @Column(nullable = false)
    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdOn;

    public Project() {
    }

    public Project(Long id, String projectCode, String name, String description, boolean active, LocalDateTime createdOn) {
        this.id = id;
        this.projectCode = projectCode;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createdOn = createdOn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @PrePersist
    public void prePersist() {
        this.createdOn = LocalDateTime.now();
    }
}

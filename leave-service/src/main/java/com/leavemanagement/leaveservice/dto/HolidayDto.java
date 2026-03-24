package com.leavemanagement.leaveservice.dto;

import java.time.LocalDate;

public class HolidayDto {
    private LocalDate date;
    private String description;

    public HolidayDto() {
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

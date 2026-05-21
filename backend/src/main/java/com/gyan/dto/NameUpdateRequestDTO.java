package com.gyan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NameUpdateRequestDTO {
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 120, message = "Name must be 120 characters or fewer")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

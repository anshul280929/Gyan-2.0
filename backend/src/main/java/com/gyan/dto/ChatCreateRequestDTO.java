package com.gyan.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatCreateRequestDTO {
    @NotBlank(message = "Chat name cannot be blank")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

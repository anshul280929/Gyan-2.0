package com.gyan.dto;

import jakarta.validation.constraints.NotBlank;

public class AskRequestDTO {
    @NotBlank(message = "Question cannot be blank")
    private String question;

    public String getQuestion() {
        return question;
    }
}

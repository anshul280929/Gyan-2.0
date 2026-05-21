package com.gyan.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRequestDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid Email Format")
    private String email;

    @NotBlank(message =  "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
        message = "Password must contain uppercase, lowercase, number, and special character"
    )
    private String password;
    
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

}

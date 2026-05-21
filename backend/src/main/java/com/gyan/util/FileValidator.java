package com.gyan.util;

import com.gyan.config.FileUploadProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidator {

    private final FileUploadProperties properties;

    public FileValidator(FileUploadProperties properties) {
        this.properties = properties;
    }

    public void validate(MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > properties.getMaxSize()) {
            throw new RuntimeException("File size exceeds the 10 MB upload limit.");
        }

        if (!properties.getAllowedTypes().contains(file.getContentType())) {
            throw new RuntimeException("Unsupported file type");
        }
    }
}

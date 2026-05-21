package com.gyan.storage;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file) throws IOException;

    Resource load(String storedFileName);

    void delete(String storedFileName);
}

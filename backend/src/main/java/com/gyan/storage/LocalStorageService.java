package com.gyan.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


@Service
public class LocalStorageService implements StorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file) throws  IOException{
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        if(!Files.exists(uploadPath)){
            Files.createDirectories(uploadPath);
        }

        String orginalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        if(orginalFilename.contains("..")) {
            throw new RuntimeException("Invalid file name");
        }

        String uniqueFileName = UUID.randomUUID() + "_" + orginalFilename;

        Path targetLocation = uploadPath.resolve(uniqueFileName);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    @Override
    public Resource load(String storedFileName){

        try {
            Path filePath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize()
                    .resolve(storedFileName);

            Resource resource;
        
            resource = new UrlResource(filePath.toUri());
        

            if(resource.exists() || resource.isReadable()) {
                return resource;
            }

        } catch (Exception e) {
           throw new RuntimeException("File not found");
        }  

        throw new RuntimeException("File not found");
    }

    @Override
    public void delete(String storedFileName) {
        try {
            Path filePath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize()
                .resolve(storedFileName);

            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new RuntimeException("Unable to delete file");
        }
    }
}

package com.gyan.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file")
public class FileUploadProperties {
    private long maxSize;

    private List<String> allowedTypes;

    public long getMaxSize() {
        return maxSize;
    }

    public List<String> getAllowedTypes() {
        return allowedTypes;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public void setAllowedTypes(List<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    
}

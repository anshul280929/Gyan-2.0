package com.gyan.processing;

import java.io.File;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

@Service
public class DocumentTextExtractionService {
    
    private final Tika tika = new Tika();

    public String extractText(String filePath) {
        try{
            File file = new File(filePath);
            tika.setMaxStringLength(500000);

            return tika.parseToString(file);
        } catch(Exception exception){
            throw new RuntimeException("Text extraction failed for file: " + filePath, exception);
        }
    }
}

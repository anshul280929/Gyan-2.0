package com.gyan.util;

import java.util.ArrayList;
import java.util.List;

public class TextChunker {
    public static List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();

        int start = 0;

        while(start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start = end;
        }

        return chunks;
    }
}

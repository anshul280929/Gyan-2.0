package com.gyan.util;

public class TextNormalizer {
    public static String normalize(String text) {
        if(text==null) {
            return null;
        }
        
        //remove line breaks
        text = text.replaceAll("\\r?\\n", " ");

        //remove multiple spaces
        text = text.replaceAll("\\s+", " ");

        //trim edges
        text = text.trim();

        return text;

    }
}

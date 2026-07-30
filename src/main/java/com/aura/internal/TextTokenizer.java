package com.aura.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Text tokenizer helper that splits text into word tokens, punctuation tokens, and whitespace.
 */
public final class TextTokenizer {

    private TextTokenizer() {
        // Prevent instantiation
    }

    /**
     * Splits a text string into individual word and punctuation tokens.
     */
    public static List<String> tokenize(String text) {
        Preconditions.checkNotNull(text, "Text cannot be null");
        List<String> tokens = new ArrayList<>();
        
        int idx = 0;
        int len = text.length();

        while (idx < len) {
            char c = text.charAt(idx);
            
            if (Character.isWhitespace(c)) {
                StringBuilder sb = new StringBuilder();
                while (idx < len && Character.isWhitespace(text.charAt(idx))) {
                    sb.append(text.charAt(idx));
                    idx++;
                }
                tokens.add(sb.toString());
            } else if (Character.isLetterOrDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (idx < len && Character.isLetterOrDigit(text.charAt(idx))) {
                    sb.append(text.charAt(idx));
                    idx++;
                }
                tokens.add(sb.toString());
            } else {
                tokens.add(String.valueOf(c));
                idx++;
            }
        }

        return tokens;
    }
}

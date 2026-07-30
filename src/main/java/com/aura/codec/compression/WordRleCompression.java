package com.aura.codec.compression;

import com.aura.internal.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Word-level dictionary compression for document text.
 * Replaces frequent words with single-byte table indexes.
 */
public class WordRleCompression {
    private static final byte WORD_MARKER = (byte) 0x00;
    private static final int MAX_DICT_SIZE = 127;

    public static byte[] compress(String text) throws IOException {
        Preconditions.checkNotNull(text, "Text cannot be null");
        if (text.isEmpty()) {
            return new byte[0];
        }

        // 1. Analyze word frequencies to build dictionary
        String[] tokens = text.split("(?<=\\b)|(?=\\b)");
        Map<String, Integer> freq = new HashMap<>();
        for (String tok : tokens) {
            if (tok.trim().length() > 2) { // Only index words longer than 2 chars
                freq.put(tok, freq.getOrDefault(tok, 0) + 1);
            }
        }

        // 2. Select top words for dictionary
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(freq.entrySet());
        sorted.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        List<String> dictionary = new ArrayList<>();
        Map<String, Byte> dictMap = new HashMap<>();

        for (int i = 0; i < Math.min(sorted.size(), MAX_DICT_SIZE); i++) {
            String word = sorted.get(i).getKey();
            dictionary.add(word);
            dictMap.put(word, (byte) i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // 3. Write Dictionary Header
        baos.write(dictionary.size());
        for (String word : dictionary) {
            byte[] wb = word.getBytes(StandardCharsets.UTF_8);
            baos.write(wb.length);
            baos.write(wb);
        }

        // 4. Compress Tokens
        for (String tok : tokens) {
            Byte idx = dictMap.get(tok);
            if (idx != null) {
                baos.write(WORD_MARKER);
                baos.write(idx);
            } else {
                byte[] raw = tok.getBytes(StandardCharsets.UTF_8);
                baos.write(raw);
            }
        }

        return baos.toByteArray();
    }

    public static String decompress(byte[] compressed) throws IOException {
        Preconditions.checkNotNull(compressed, "Compressed data cannot be null");
        if (compressed.length == 0) {
            return "";
        }

        int idx = 0;
        int dictSize = compressed[idx++] & 0xFF;
        List<String> dictionary = new ArrayList<>();

        for (int i = 0; i < dictSize; i++) {
            int len = compressed[idx++] & 0xFF;
            if (idx + len > compressed.length) {
                throw new IllegalArgumentException("Truncated word dictionary data");
            }
            String word = new String(compressed, idx, len, StandardCharsets.UTF_8);
            dictionary.add(word);
            idx += len;
        }

        StringBuilder sb = new StringBuilder();
        while (idx < compressed.length) {
            byte b = compressed[idx];
            if (b == WORD_MARKER) {
                if (idx + 1 >= compressed.length) {
                    throw new IllegalArgumentException("Truncated word dictionary index");
                }
                int dictIdx = compressed[idx + 1] & 0xFF;
                if (dictIdx >= dictionary.size()) {
                    throw new IllegalArgumentException("Invalid word dictionary index: " + dictIdx);
                }
                sb.append(dictionary.get(dictIdx));
                idx += 2;
            } else {
                // Find next marker or EOF to decode direct text
                int start = idx;
                while (idx < compressed.length && compressed[idx] != WORD_MARKER) {
                    idx++;
                }
                sb.append(new String(compressed, start, idx - start, StandardCharsets.UTF_8));
            }
        }

        return sb.toString();
    }
}

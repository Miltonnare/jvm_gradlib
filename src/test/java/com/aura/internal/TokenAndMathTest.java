package com.aura.internal;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TokenAndMathTest {

    @Test
    public void testTextTokenizer() {
        String text = "Hello, World 123!";
        List<String> tokens = TextTokenizer.tokenize(text);
        
        assertNotNull(tokens);
        assertTrue(tokens.contains("Hello"));
        assertTrue(tokens.contains("World"));
        assertTrue(tokens.contains("123"));
        assertTrue(tokens.contains(","));
        assertTrue(tokens.contains("!"));
    }

    @Test
    public void testMathClamping() {
        assertEquals(5, MathUtils.clamp(10, 0, 5));
        assertEquals(0, MathUtils.clamp(-10, 0, 5));
        assertEquals(3, MathUtils.clamp(3, 0, 5));

        assertEquals(2.5, MathUtils.clamp(2.5, 0.0, 5.0));
    }
}

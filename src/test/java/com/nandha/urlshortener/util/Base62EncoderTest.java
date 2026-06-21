package com.nandha.urlshortener.util;

import org.junit.jupiter.api.Test;

// Import assertion methods like assertEquals(), assertThrows(), etc.
import static org.junit.jupiter.api.Assertions.*;

// eg to read the test.
//@Test
//void shouldDoSomething() {
//
//    // Arrange
//    // Prepare inputs
//
//    // Act
//    // Call the method
//
//    // Assert
//    // Verify the result
//}

class Base62EncoderTest {

    @Test // Marks this method as a test case
    void shouldEncodeZero() {
        // Arrange
        // Input value we want to test
        long input = 0;

        // Act
        // Call the method being tested
        String result = Base62Encoder.encode(input);

        // Assert
        // Verify the actual result matches the expected result
        assertEquals("0", result);
    }

    @Test
    void shouldEncodeOne() {

        // Arrange
        long input = 1;

        // Act
        String result = Base62Encoder.encode(input);

        // Assert
        assertEquals("1", result);
    }

    @Test
    void shouldEncodeSixtyOne() {
        // Arrange
        long input = 61;

        // Act
        String result = Base62Encoder.encode(input);

        // Assert
        // In Base62, 61 is represented by 'z'
        assertEquals("z", result);
    }

    @Test
    void shouldEncodeSixtyTwo() {
        // Arrange
        long input = 62;

        // Act
        String result = Base62Encoder.encode(input);

        // Assert
        // 62 in Base62 becomes "10"
        assertEquals("10", result);
    }

    @Test
    void shouldEncodeLargeNumber() {
        // Arrange
        long input = 123456789;

        // Act
        String result = Base62Encoder.encode(input);

        // Assert

        // Check that the method didn't return null
        assertNotNull(result);

        // Check that the returned string is not empty
        assertFalse(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionForNegativeNumber() {
        // Assert

        // Verify that calling encode(-1)
        // throws IllegalArgumentException

        assertThrows(
                IllegalArgumentException.class,
                () -> Base62Encoder.encode(-1)
        );
    }
}
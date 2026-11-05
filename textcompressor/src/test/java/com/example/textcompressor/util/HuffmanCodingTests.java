package com.example.textcompressor.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class HuffmanCodingTests {
    @Test
    void roundTripsTextWithMultipleSymbols() {
        String input = "hello, Huffman! 🌍";
        HuffmanCoding.EncodedText encoded = HuffmanCoding.encode(input);

        assertEquals(input, HuffmanCoding.decode(encoded.bits(), encoded.codeTable()));
    }

    @Test
    void handlesEmptyInput() {
        HuffmanCoding.EncodedText encoded = HuffmanCoding.encode("");

        assertEquals("", encoded.bits());
        assertEquals("", encoded.codeTable());
        assertEquals("", HuffmanCoding.decode(encoded.bits(), encoded.codeTable()));
    }

    @Test
    void assignsNonEmptyCodeToSingleRepeatedSymbol() {
        HuffmanCoding.EncodedText encoded = HuffmanCoding.encode("aaaa");

        assertEquals("0000", encoded.bits());
        assertEquals("aaaa", HuffmanCoding.decode(encoded.bits(), encoded.codeTable()));
    }

    @Test
    void eachEncodingUsesItsOwnCodeTable() {
        HuffmanCoding.EncodedText first = HuffmanCoding.encode("aaaa");
        HuffmanCoding.EncodedText second = HuffmanCoding.encode("bbbb");

        assertNotEquals(first.codeTable(), second.codeTable());
        assertEquals("aaaa", HuffmanCoding.decode(first.bits(), first.codeTable()));
        assertEquals("bbbb", HuffmanCoding.decode(second.bits(), second.codeTable()));
    }
}

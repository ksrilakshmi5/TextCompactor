package com.example.textcompressor.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class HuffmanFileCodecTests {
    @Test
    void roundTripsUtf8TextBytes() {
        byte[] source = "Huffman coding can compress repeated words. 🌍\n".repeat(80)
                .getBytes(StandardCharsets.UTF_8);

        byte[] compressed = HuffmanFileCodec.compress(source);

        assertArrayEquals(source, HuffmanFileCodec.decompress(compressed));
    }

    @Test
    void compressesHighlyRepetitiveFileSmallerThanSource() {
        byte[] source = "A".repeat(20_000).getBytes(StandardCharsets.UTF_8);

        byte[] compressed = HuffmanFileCodec.compress(source);

        assertTrue(compressed.length < source.length);
        assertArrayEquals(source, HuffmanFileCodec.decompress(compressed));
    }

    @Test
    void handlesEmptyAndSingleSymbolFiles() {
        byte[] empty = new byte[0];
        byte[] singleSymbol = new byte[10_000];
        Arrays.fill(singleSymbol, (byte) 0xE2);

        assertArrayEquals(empty, HuffmanFileCodec.decompress(HuffmanFileCodec.compress(empty)));
        assertArrayEquals(singleSymbol,
                HuffmanFileCodec.decompress(HuffmanFileCodec.compress(singleSymbol)));
    }

    @Test
    void rejectsInvalidArchive() {
        assertThrows(IllegalArgumentException.class,
                () -> HuffmanFileCodec.decompress("not an archive".getBytes(StandardCharsets.UTF_8)));
    }
}

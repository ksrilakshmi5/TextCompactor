package com.example.textcompressor.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.StringJoiner;

/** Huffman encoder that produces a bit string and its code table. */
public final class HuffmanCoding {
    private HuffmanCoding() {
    }

    private static final class Node {
        private final int symbol;
        private final int frequency;
        private final int smallestSymbol;
        private final Node left;
        private final Node right;

        private Node(int symbol, int frequency) {
            this(symbol, frequency, symbol, null, null);
        }

        private Node(int frequency, Node left, Node right) {
            this(-1, frequency, Math.min(left.smallestSymbol, right.smallestSymbol), left, right);
        }

        private Node(int symbol, int frequency, int smallestSymbol, Node left, Node right) {
            this.symbol = symbol;
            this.frequency = frequency;
            this.smallestSymbol = smallestSymbol;
            this.left = left;
            this.right = right;
        }

        private boolean isLeaf() {
            return left == null && right == null;
        }
    }

    /** The encoded bits and a compact symbol-to-code table (Unicode code point = bit code). */
    public record EncodedText(String bits, String codeTable) {
    }

    public static EncodedText encode(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text must not be null.");
        }
        if (text.isEmpty()) {
            return new EncodedText("", "");
        }

        Map<Integer, Integer> frequencies = new HashMap<>();
        text.codePoints().forEach(symbol -> frequencies.merge(symbol, 1, Integer::sum));

        Comparator<Node> nodeOrder = Comparator.comparingInt((Node node) -> node.frequency)
                .thenComparingInt(node -> node.smallestSymbol);
        PriorityQueue<Node> nodes = new PriorityQueue<>(nodeOrder);
        frequencies.forEach((symbol, frequency) -> nodes.add(new Node(symbol, frequency)));

        while (nodes.size() > 1) {
            Node left = nodes.remove();
            Node right = nodes.remove();
            nodes.add(new Node(left.frequency + right.frequency, left, right));
        }

        Map<Integer, String> codes = new HashMap<>();
        buildCodes(nodes.remove(), "", codes);

        StringBuilder bits = new StringBuilder();
        text.codePoints().forEach(symbol -> bits.append(codes.get(symbol)));
        return new EncodedText(bits.toString(), serializeCodeTable(codes));
    }

    /** Decodes bits using the code table returned by {@link #encode(String)}. */
    public static String decode(String bits, String codeTable) {
        if (bits == null || codeTable == null) {
            throw new IllegalArgumentException("Encoded bits and code table must not be null.");
        }
        if (bits.isEmpty()) {
            return "";
        }

        Map<String, Integer> symbolsByCode = new HashMap<>();
        for (String entry : codeTable.split(";")) {
            int separator = entry.indexOf('=');
            if (separator <= 0 || separator == entry.length() - 1) {
                throw new IllegalArgumentException("Invalid Huffman code table.");
            }
            int symbol;
            try {
                symbol = Integer.parseInt(entry.substring(0, separator));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid symbol in Huffman code table.", exception);
            }
            symbolsByCode.put(entry.substring(separator + 1), symbol);
        }
        if (symbolsByCode.isEmpty()) {
            throw new IllegalArgumentException("A code table is required to decode non-empty bits.");
        }

        StringBuilder decoded = new StringBuilder();
        StringBuilder code = new StringBuilder();
        for (int index = 0; index < bits.length(); index++) {
            char bit = bits.charAt(index);
            if (bit != '0' && bit != '1') {
                throw new IllegalArgumentException("Encoded data must contain only 0 and 1.");
            }
            code.append(bit);
            Integer symbol = symbolsByCode.get(code.toString());
            if (symbol != null) {
                decoded.appendCodePoint(symbol);
                code.setLength(0);
            }
        }
        if (!code.isEmpty()) {
            throw new IllegalArgumentException("Encoded bits do not match the supplied code table.");
        }
        return decoded.toString();
    }

    private static void buildCodes(Node node, String prefix, Map<Integer, String> codes) {
        if (node.isLeaf()) {
            // A one-symbol input still needs a non-empty code for each occurrence.
            codes.put(node.symbol, prefix.isEmpty() ? "0" : prefix);
            return;
        }
        buildCodes(node.left, prefix + '0', codes);
        buildCodes(node.right, prefix + '1', codes);
    }

    private static String serializeCodeTable(Map<Integer, String> codes) {
        StringJoiner table = new StringJoiner(";");
        codes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> table.add(entry.getKey() + "=" + entry.getValue()));
        return table.toString();
    }
}

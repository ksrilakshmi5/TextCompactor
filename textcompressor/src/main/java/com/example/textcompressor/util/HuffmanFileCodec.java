package com.example.textcompressor.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/** Packs Huffman-coded bytes into a self-contained archive. */
public final class HuffmanFileCodec {
    private static final byte[] MAGIC = {'H', 'U', 'F', '1'};
    private static final int SYMBOL_COUNT = 256;

    private HuffmanFileCodec() {
    }

    private static final class Node {
        private final int symbol;
        private final long frequency;
        private final int smallestSymbol;
        private final Node left;
        private final Node right;

        private Node(int symbol, long frequency) {
            this(symbol, frequency, symbol, null, null);
        }

        private Node(Node left, Node right) {
            this(-1, left.frequency + right.frequency,
                    Math.min(left.smallestSymbol, right.smallestSymbol), left, right);
        }

        private Node(int symbol, long frequency, int smallestSymbol, Node left, Node right) {
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

    public static byte[] compress(byte[] source) {
        if (source == null) {
            throw new IllegalArgumentException("Source file data must not be null.");
        }

        int[] frequencies = new int[SYMBOL_COUNT];
        for (byte value : source) {
            frequencies[Byte.toUnsignedInt(value)]++;
        }

        int symbolCount = 0;
        for (int frequency : frequencies) {
            if (frequency > 0) {
                symbolCount++;
            }
        }

        byte[] payload = new byte[0];
        if (symbolCount > 1) {
            Node root = buildTree(frequencies);
            String[] codes = buildCodes(root);
            BitWriter writer = new BitWriter();
            for (byte value : source) {
                writer.write(codes[Byte.toUnsignedInt(value)]);
            }
            payload = writer.finish();
        }

        try {
            ByteArrayOutputStream archiveBytes = new ByteArrayOutputStream();
            DataOutputStream archive = new DataOutputStream(archiveBytes);
            archive.write(MAGIC);
            archive.writeLong(source.length);
            archive.writeShort(symbolCount);
            for (int symbol = 0; symbol < frequencies.length; symbol++) {
                if (frequencies[symbol] > 0) {
                    archive.writeByte(symbol);
                    archive.writeInt(frequencies[symbol]);
                }
            }
            archive.write(payload);
            archive.flush();
            return archiveBytes.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create Huffman archive.", exception);
        }
    }

    public static byte[] decompress(byte[] archiveBytes) {
        if (archiveBytes == null) {
            throw new IllegalArgumentException("Archive data must not be null.");
        }

        try {
            DataInputStream archive = new DataInputStream(new ByteArrayInputStream(archiveBytes));
            byte[] magic = archive.readNBytes(MAGIC.length);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new IllegalArgumentException("This is not a supported Huffman archive.");
            }

            long originalLength = archive.readLong();
            if (originalLength < 0 || originalLength > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("The uncompressed file size is not supported.");
            }

            int symbolCount = archive.readUnsignedShort();
            if (symbolCount > SYMBOL_COUNT) {
                throw new IllegalArgumentException("The archive contains an invalid symbol table.");
            }

            int[] frequencies = new int[SYMBOL_COUNT];
            long frequencyTotal = 0;
            for (int entry = 0; entry < symbolCount; entry++) {
                int symbol = archive.readUnsignedByte();
                int frequency = archive.readInt();
                if (frequency <= 0 || frequencies[symbol] != 0) {
                    throw new IllegalArgumentException("The archive contains an invalid frequency table.");
                }
                frequencies[symbol] = frequency;
                frequencyTotal += frequency;
            }

            if (frequencyTotal != originalLength || (originalLength == 0) != (symbolCount == 0)) {
                throw new IllegalArgumentException("The archive metadata is inconsistent.");
            }
            if (originalLength == 0) {
                if (archive.available() != 0) {
                    throw new IllegalArgumentException("The archive contains unexpected data.");
                }
                return new byte[0];
            }

            Node root = buildTree(frequencies);
            long expectedPayloadLength = 0;
            if (!root.isLeaf()) {
                String[] codes = buildCodes(root);
                long bitCount = 0;
                for (int symbol = 0; symbol < frequencies.length; symbol++) {
                    if (frequencies[symbol] > 0) {
                        bitCount += (long) frequencies[symbol] * codes[symbol].length();
                    }
                }
                expectedPayloadLength = (bitCount + 7) / 8;
            }
            if (archive.available() != expectedPayloadLength) {
                throw new IllegalArgumentException("The archive payload is incomplete or invalid.");
            }

            byte[] restored = new byte[(int) originalLength];
            int[] restoredFrequencies = new int[SYMBOL_COUNT];
            if (root.isLeaf()) {
                Arrays.fill(restored, (byte) root.symbol);
                restoredFrequencies[root.symbol] = restored.length;
            } else {
                BitReader reader = new BitReader(archive);
                for (int index = 0; index < restored.length; index++) {
                    Node node = root;
                    while (!node.isLeaf()) {
                        int bit = reader.read();
                        if (bit < 0) {
                            throw new IllegalArgumentException("The archive payload ended unexpectedly.");
                        }
                        node = bit == 0 ? node.left : node.right;
                    }
                    restored[index] = (byte) node.symbol;
                    restoredFrequencies[node.symbol]++;
                }
            }

            if (!Arrays.equals(frequencies, restoredFrequencies)) {
                throw new IllegalArgumentException("The archive data does not match its frequency table.");
            }
            return restored;
        } catch (EOFException exception) {
            throw new IllegalArgumentException("The Huffman archive is truncated.", exception);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read the Huffman archive.", exception);
        }
    }

    private static Node buildTree(int[] frequencies) {
        Comparator<Node> order = Comparator.comparingLong((Node node) -> node.frequency)
                .thenComparingInt(node -> node.smallestSymbol);
        PriorityQueue<Node> nodes = new PriorityQueue<>(order);
        for (int symbol = 0; symbol < frequencies.length; symbol++) {
            if (frequencies[symbol] > 0) {
                nodes.add(new Node(symbol, frequencies[symbol]));
            }
        }
        while (nodes.size() > 1) {
            nodes.add(new Node(nodes.remove(), nodes.remove()));
        }
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("The archive does not contain any symbols.");
        }
        return nodes.remove();
    }

    private static String[] buildCodes(Node root) {
        String[] codes = new String[SYMBOL_COUNT];
        buildCodes(root, "", codes);
        return codes;
    }

    private static void buildCodes(Node node, String prefix, String[] codes) {
        if (node.isLeaf()) {
            codes[node.symbol] = prefix.isEmpty() ? "0" : prefix;
            return;
        }
        buildCodes(node.left, prefix + '0', codes);
        buildCodes(node.right, prefix + '1', codes);
    }

    private static final class BitWriter {
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private int currentByte;
        private int bitCount;

        private void write(String code) {
            for (int index = 0; index < code.length(); index++) {
                currentByte = (currentByte << 1) | (code.charAt(index) - '0');
                if (++bitCount == 8) {
                    output.write(currentByte);
                    currentByte = 0;
                    bitCount = 0;
                }
            }
        }

        private byte[] finish() {
            if (bitCount > 0) {
                output.write(currentByte << (8 - bitCount));
            }
            return output.toByteArray();
        }
    }

    private static final class BitReader {
        private final DataInputStream input;
        private int currentByte;
        private int remainingBits;

        private BitReader(DataInputStream input) {
            this.input = input;
        }

        private int read() throws IOException {
            if (remainingBits == 0) {
                currentByte = input.read();
                if (currentByte < 0) {
                    return -1;
                }
                remainingBits = 8;
            }
            return (currentByte >> --remainingBits) & 1;
        }
    }
}

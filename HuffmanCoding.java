package com.example.textcompressor.util;

import java.util.*;

public class HuffmanCoding {
    private static class Node implements Comparable<Node> {
        Character data;
        int cost;
        Node left;
        Node right;

        public Node(Character data, int cost) {
            this.data = data;
            this.cost = cost;
            this.left = null;
            this.right = null;
        }

        @Override
        public int compareTo(Node other) {
            return this.cost - other.cost;
        }
    }

    private static HashMap<Character, String> encoder = new HashMap<>();
    private static HashMap<String, Character> decoder = new HashMap<>();

    public static String encode(String str) {
        HashMap<Character, Integer> fmap = new HashMap<>();
        for (char ch : str.toCharArray()) {
            fmap.put(ch, fmap.getOrDefault(ch, 0) + 1);
        }

        PriorityQueue<Node> minHeap = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : fmap.entrySet()) {
            Node node = new Node(entry.getKey(), entry.getValue());
            minHeap.add(node);
        }

        while (minHeap.size() != 1) {
            Node first = minHeap.remove();
            Node second = minHeap.remove();
            Node newNode = new Node('\0', first.cost + second.cost);
            newNode.left = first;
            newNode.right = second;
            minHeap.add(newNode);
        }

        Node root = minHeap.remove();
        initEncoderDecoder(root, "");

        StringBuilder encodedString = new StringBuilder();
        for (char ch : str.toCharArray()) {
            encodedString.append(encoder.get(ch));
        }
        return encodedString.toString();
    }

    private static void initEncoderDecoder(Node node, String ofs) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            encoder.put(node.data, ofs);
            decoder.put(ofs, node.data);
        }
        initEncoderDecoder(node.left, ofs + '0');
        initEncoderDecoder(node.right, ofs + '1');
    }
}

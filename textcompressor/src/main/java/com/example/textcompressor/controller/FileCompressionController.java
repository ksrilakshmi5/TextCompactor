package com.example.textcompressor.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.textcompressor.util.HuffmanFileCodec;

@RestController
@RequestMapping("/api/files")
public class FileCompressionController {
    @PostMapping(value = "/compress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> compressFile(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            byte[] original = file.getBytes();
            byte[] compressed = HuffmanFileCodec.compress(original);
            HttpHeaders headers = attachmentHeaders("compressed.huff");
            headers.set("X-Original-Size", Long.toString(original.length));
            headers.set("X-Compressed-Size", Long.toString(compressed.length));
            return ResponseEntity.ok().headers(headers).body(compressed);
        } catch (IllegalArgumentException exception) {
            return badRequest(exception);
        }
    }

    @PostMapping(value = "/decompress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> decompressFile(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            byte[] restored = HuffmanFileCodec.decompress(file.getBytes());
            return ResponseEntity.ok()
                    .headers(attachmentHeaders("decompressed.txt"))
                    .body(restored);
        } catch (IllegalArgumentException exception) {
            return badRequest(exception);
        }
    }

    private HttpHeaders attachmentHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return headers;
    }

    private ResponseEntity<byte[]> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.TEXT_PLAIN)
                .body(exception.getMessage().getBytes(StandardCharsets.UTF_8));
    }
}

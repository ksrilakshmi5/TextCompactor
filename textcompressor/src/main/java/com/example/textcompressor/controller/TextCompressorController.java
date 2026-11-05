package com.example.textcompressor.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.textcompressor.model.TextCompressorModel;
import com.example.textcompressor.service.TextCompressorService;

import java.util.List;

@RestController
@RequestMapping("/api/text")
public class TextCompressorController {
    private final TextCompressorService service;
    public TextCompressorController(TextCompressorService service) {
        this.service = service;
    }

    @PostMapping(value = "/compress", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TextCompressorModel compressText(@RequestBody TextCompressorModel request) {
        return service.saveTextRequest(request);
    }

    @PostMapping(value = "/compress", consumes = MediaType.TEXT_PLAIN_VALUE)
    public TextCompressorModel compressPlainText(@RequestBody String text) {
        TextCompressorModel request = new TextCompressorModel();
        request.setText(text);
        return service.saveTextRequest(request);
    }

    @GetMapping({"/requests"})
    public List<TextCompressorModel> getAllRequests() {
        return service.getAllRequests();
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        if (!service.deleteRequest(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

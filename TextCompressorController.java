package com.example.textcompressor.controller;

import com.example.textcompressor.model.TextRequest;
import com.example.textcompressor.service.TextCompressorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/text")
public class TextCompressorController {
    private final TextCompressorService service;

    @Autowired
    public TextCompressorController(TextCompressorService service) {
        this.service = service;
    }

    @PostMapping("/compress")
    public TextRequest compressText(@RequestBody TextRequest request) {
        return service.saveTextRequest(request);
    }

    @GetMapping("/requests")
    public List<TextRequest> getAllRequests() {
        return service.getAllRequests();
    }
}

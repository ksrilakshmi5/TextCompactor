package com.example.textcompressor.service;

import com.example.textcompressor.model.TextRequest;
import com.example.textcompressor.repository.TextCompressorRepository;
import com.example.textcompressor.util.HuffmanCoding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TextCompressorService {
    private final TextCompressorRepository repository;

    @Autowired
    public TextCompressorService(TextCompressorRepository repository) {
        this.repository = repository;
    }

    public TextRequest saveTextRequest(TextRequest request) {
        String encoded = HuffmanCoding.encode(request.getText());
        request.setEncodedText(encoded);
        return repository.save(request);
    }

    public List<TextRequest> getAllRequests() {
        return repository.findAll();
    }
}

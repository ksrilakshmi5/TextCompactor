package com.example.textcompressor.service;
import com.example.textcompressor.util.HuffmanCoding;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

import com.example.textcompressor.model.TextCompressorModel;
import com.example.textcompressor.repository.TextCompressorRepository;

@Service
public class TextCompressorService {
    private final TextCompressorRepository repository;

    public TextCompressorService(TextCompressorRepository repository) {
        this.repository = repository;
    }

    public TextCompressorModel saveTextRequest(TextCompressorModel request) {
        if (request == null || request.getText() == null) {
            throw new IllegalArgumentException("Request text must not be null.");
        }
        HuffmanCoding.EncodedText encoded = HuffmanCoding.encode(request.getText());
        request.setEncodedText(encoded.bits());
        request.setCodeTable(encoded.codeTable());
        return repository.save(request);
    }
    
    public List<TextCompressorModel> getAllRequests() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public boolean deleteRequest(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
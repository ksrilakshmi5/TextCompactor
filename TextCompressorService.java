package com.example.textcompressor.service;

import com.example.textcompressor.model.TextRequest;
import com.example.textcompressor.util.HuffmanCoding;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextCompressorService {
    
    private List<TextRequest> textRequests = new ArrayList<>();

    // Method to save a text request and compress it
    public TextRequest saveTextRequest(TextRequest request) {
        
        String encoded = HuffmanCoding.encode(request.getText());
        request.setEncodedText(encoded);

      
        textRequests.add(request);

        return request;
    }

    // Method to get all saved requests
    public List<TextRequest> getAllRequests() {
        return textRequests;
    }
}

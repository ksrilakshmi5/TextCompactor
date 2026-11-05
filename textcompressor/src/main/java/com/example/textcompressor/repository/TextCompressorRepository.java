package com.example.textcompressor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.textcompressor.model.TextCompressorModel;

public interface TextCompressorRepository extends JpaRepository<TextCompressorModel, Long> {
}

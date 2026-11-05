package com.example.textcompressor.model;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
@Entity
@Table(name = "text_requests")

public class TextCompressorModel {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
  
    @Lob
    @Column(nullable = false)
      private String text;
  
    @Lob
    @Column(nullable = false)
      private String encodedText;

    @Lob
    private String codeTable;
  
      public Long getId() {
          return id;
      }
  
      public void setId(Long id) {
          this.id = id;
      }
  
      public String getText() {
          return text;
      }
  
      public void setText(String text) {
          this.text = text;
      }
  
      public String getEncodedText() {
          return encodedText;
      }
  
      public void setEncodedText(String encodedText) {
          this.encodedText = encodedText;
    }

      public String getCodeTable() {
          return codeTable;
      }

      public void setCodeTable(String codeTable) {
          this.codeTable = codeTable;
      }
}




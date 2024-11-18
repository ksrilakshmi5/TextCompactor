//NO DATABASE CONNECTED IF IN FUTURE REQUIRED
package com.example.textcompressor.repository;
import com.example.textcompressor.model.TextRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TextCompressorRepository extends JpaRepository<TextRequest, Long> {

}

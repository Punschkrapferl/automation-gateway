package com.example.automationgateway.repository;

import com.example.automationgateway.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, String> {
}

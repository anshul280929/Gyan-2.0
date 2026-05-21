package com.gyan.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.TimeZone;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.gyan.entity.Document;
import com.gyan.repository.DocumentRepository;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DocumentRepositoryTest {

    @BeforeAll
    static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void testSaveDocument() {

        Document doc = new Document();
        doc.setFilename("test.txt");

        Document saved = documentRepository.save(doc);

        assertNotNull(saved.getId());
    }
}   
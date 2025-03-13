package com.codingapi.rag.etl;

import lombok.AllArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class DocumentService {

    private final LocalJsonReader localJsonReader;
    private final TokenTextSplitter splitter = new TokenTextSplitter();
    private final VectorStore vectorStore;

    public void importDocuments() {
        List<Document> documents = localJsonReader.loadDocument();
        documents = splitter.apply(documents);

        vectorStore.add(documents);

        String testQuestion = "Trek";
        SearchRequest request = SearchRequest.builder()
                .query(testQuestion)
                .similarityThreshold(0.65)
                .build();
        List<Document> searchDocuments = vectorStore.similaritySearch(request);


        System.out.println("Test Question: " + testQuestion);
        System.out.println("searchDocuments: " + searchDocuments);
    }

}

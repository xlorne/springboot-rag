package com.codingapi.rag.etl;

import lombok.AllArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
public class DocumentService {

    private final LocalJsonReader localJsonReader;
    private final TokenTextSplitter splitter = new TokenTextSplitter();
    private final VectorStore vectorStore;


    private Document reBuild(Document document) {
        String text = Objects.requireNonNull(document.getText());
        String id = DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
        return new Document(id, text, document.getMetadata());
    }

    public void importDocuments() {
        System.out.println("<-----------------importDocuments--------------------->");
        List<Document> documents = localJsonReader.loadDocument();
        documents = splitter.apply(documents);
        List<Document> data =  documents.stream().map(this::reBuild).toList();
        vectorStore.add(data);

        String testQuestion = "Trek";
        SearchRequest request = SearchRequest.builder()
                .query(testQuestion)
                .similarityThreshold(0.65)
                .build();
        List<Document> searchDocuments = vectorStore.similaritySearch(request);


        System.out.println("Test Question: " + testQuestion);
        assert searchDocuments != null;
        System.out.println("searchDocuments size: " + searchDocuments.size());
        System.out.println("searchDocuments: " + searchDocuments);

        System.out.println(">-----------------importDocuments---------------------<");
    }

}

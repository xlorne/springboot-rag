package com.codingapi.rag.etl;

import lombok.AllArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
public class DocumentService {

    private final LocalJsonReader localJsonReader;
    private final TokenTextSplitter splitter = new TokenTextSplitter();
    private final VectorStore vectorStore;


    private Document reBuild(Document document) {
        String text = Objects.requireNonNull(document.getText());
        // json all data will be used as metadata
        Map<String,Object> metadata = document.getMetadata();

        return Document.builder()
                .id(metadata.get("id").toString()) // id is required and must be unique
//                .id(DigestUtils.md5Hex(text)) // if id is not provided, can will be generated from text to md5
                .text(text)
                .metadata(metadata)
                .media(document.getMedia())
                .build();
    }

    public void importDocuments() {
        System.out.println("<-----------------importDocuments--------------------->");
        List<Document> documents = localJsonReader.loadDocument();
        documents = splitter.apply(documents);
        List<Document> data =  documents.stream().map(this::reBuild).toList();

        vectorStore.delete(data.stream().map(Document::getId).toList());
        // add data to vector store
        // vector field
        // 1. doc_id : document id
        // 2. content : document text
        // 3. metadata : document metadata
        // 4. embedding : document text to embedding, by embedding model
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

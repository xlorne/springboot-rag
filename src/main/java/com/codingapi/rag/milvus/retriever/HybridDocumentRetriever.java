package com.codingapi.rag.milvus.retriever;

import com.codingapi.rag.milvus.MilvusServerClient;
import lombok.AllArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

import java.util.List;

@AllArgsConstructor
public class HybridDocumentRetriever implements DocumentRetriever {

    private final MilvusServerClient milvusServerClient;
    private final EmbeddingModel embeddingModel;

    @Override
    public List<Document> retrieve(Query query) {
        return milvusServerClient.hybridSearch(query.text(), embeddingModel);
    }
}

package com.codingapi.rag.milvus.store;

import com.codingapi.rag.milvus.MilvusServerClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.*;

public class MilvusVectorStore implements VectorStore {

    private final MilvusServerClient milvusServerClient;
    private final EmbeddingModel embeddingModel;

    public MilvusVectorStore(MilvusServerClient milvusServerClient,
                             EmbeddingModel embeddingModel) {
        this.milvusServerClient = milvusServerClient;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void add(List<Document> documents) {
        List<JsonObject> data = new ArrayList<>();
        for (Document document : documents) {
            float[] dense = embeddingModel.embed(document);
            SortedMap<Long, Float> sparseMap = new TreeMap<>();
            for (int i = 0; i < dense.length; i++) {
                sparseMap.put((long) i, Math.max(0, dense[i]));
            }

            Gson gson = new Gson();
            JsonObject row = new JsonObject();
            row.addProperty("id", 1);
            row.addProperty("text", document.getText());
            row.add("dense", gson.toJsonTree(dense));
            row.add("sparse", gson.toJsonTree(sparseMap));
            data.add(row);
        }
        milvusServerClient.insert(data);
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        return milvusServerClient.search(request, embeddingModel);
    }

    @Override
    public void delete(List<String> documentIds) {
        milvusServerClient.delete(documentIds);
    }

    @Override
    public void delete(Filter.Expression filterExpression) {
        milvusServerClient.delete(filterExpression);
    }
}

package com.codingapi.rag;

import com.codingapi.rag.interceptor.LoggingInterceptor;
import com.codingapi.rag.milvus.MilvusServerClient;
import com.codingapi.rag.milvus.properties.MilvusProperties;
import com.codingapi.rag.milvus.retriever.HybridDocumentRetriever;
import com.codingapi.rag.milvus.store.MilvusVectorStore;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RAGConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "codingapi.milvus")
    public MilvusProperties milvusProperties() {
        return new MilvusProperties();
    }

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public RestClient.Builder restClient() {
        return RestClient.builder()
                .requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
                .requestInterceptor(new LoggingInterceptor(true));
    }

    @Bean
    public MilvusServerClient milvusServerClient(MilvusProperties milvusProperties) {
        return new MilvusServerClient(milvusProperties);
    }

    @Bean
    public VectorStore vectorStore(MilvusServerClient milvusServerClient, EmbeddingModel embeddingModel) {
        return new MilvusVectorStore(milvusServerClient, embeddingModel);
    }

    @Bean
    public HybridDocumentRetriever hybridDocumentRetriever(MilvusServerClient milvusServerClient, EmbeddingModel embeddingModel) {
        return new HybridDocumentRetriever(milvusServerClient, embeddingModel);
    }
}

package com.codingapi.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
public class ChatService {

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    private final static int CHAT_MEMORY_RETRIEVE_SIZE = 1000;

    public ChatService(ChatClient.Builder builder, ChatMemory chatMemory, VectorStore vectorStore) {
        this.vectorStore = vectorStore;

        this.chatClient = builder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new QuestionAnswerAdvisor(vectorStore)
                )
                .build();
    }


    public String question(String chatId,String userMessage) {
        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(
                        VectorStoreDocumentRetriever.builder()
                                .similarityThreshold(0.50)
                                .vectorStore(vectorStore)
                                .build())
                .build();
        try {
            ChatClient.CallResponseSpec responseSpec = chatClient.prompt()
                    .advisors(retrievalAugmentationAdvisor)
                    .advisors(
                            a -> a
                                    .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                    .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE)
                    )
                    .user(userMessage)
                    .call();
            ChatResponse chatResponse = responseSpec.chatResponse();
            if (chatResponse != null) {
                Generation generation = chatResponse.getResult();
                return generation.getOutput().getText();
            }
            throw new RuntimeException("question response was null");
        } catch (Exception e) {
            throw new RuntimeException("question response was error", e);
        }
    }
}

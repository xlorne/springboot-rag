package com.codingapi.rag.service;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
public class ChatService {

    private final ChatClient chatClient;

    private final static int CHAT_MEMORY_RETRIEVE_SIZE = 1000;

    private final static String CHAT_MEMORY_PROMPT = """
            这是历史的对话数据：
            ---------------------
            MEMORY:
            {memory}
            ---------------------
            """;

    public ChatService(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultAdvisors(
                        new PromptChatMemoryAdvisor(chatMemory, CHAT_MEMORY_PROMPT)
                )
                .build();
    }


    public String ask(String chatId, String userMessage) {
        try {
            ChatClient.CallResponseSpec responseSpec = chatClient.prompt()
                    .user(userMessage)
                    .advisors(
                            a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                    .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE)
                    )
                    .call();
            ChatResponse chatResponse = responseSpec.chatResponse();
            if (chatResponse != null) {
                Generation generation = chatResponse.getResult();
                return generation.getOutput().getText();
            }
            throw new RuntimeException("ask response was null");
        } catch (Exception e) {
            throw new RuntimeException("ask response was error", e);
        }
    }
}

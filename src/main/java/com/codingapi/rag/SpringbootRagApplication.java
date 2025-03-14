package com.codingapi.rag;

import com.codingapi.rag.etl.DocumentService;
import com.codingapi.rag.service.ChatService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringbootRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootRagApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ChatService chatService,
                                               DocumentService documentService,
                                               ConfigurableApplicationContext ctx) {

        return args -> {
            documentService.importDocuments();

            String chatId = "1";

            String question = "What is Trek ?";
            String answer = chatService.question(chatId,question);
            System.out.println("question: " + question);
            System.out.println("assistant: " + answer);

            ctx.close();
        };
    }
}

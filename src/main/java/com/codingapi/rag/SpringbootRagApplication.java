package com.codingapi.rag;

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
    public CommandLineRunner commandLineRunner(ChatService chatService,ConfigurableApplicationContext ctx) {
        return args -> {
            String answer = chatService.ask("1","你好");
            System.out.println(answer);
            ctx.close();
        };
    }
}

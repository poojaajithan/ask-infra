package com.askinfra.backend.controllers;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RestController;

import com.askinfra.backend.models.ChatRequest;
import com.askinfra.backend.models.ChatResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class ChatController {

    private final ChatClient chatClient;

    ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/api/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String selectedMode = resolveMode(request.getMode());

        String reply = chatClient
                        .prompt()
                        .system(systemPromptFor(selectedMode))
                        .user(request.getMessage())
                        .call()
                        .content();
        
        return ChatResponse.builder()
                .reply(reply)
                .mode(selectedMode)
                .sources(List.of())
                .build();
    }

    private String resolveMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return "chat";
        }
        
        return switch(mode.toLowerCase()) {
            case "chat", "explain", "ops" -> mode.toLowerCase();
            default -> "chat";
        };
    }

    private String systemPromptFor(String mode) {
        return switch (mode) {
            case "explain" -> """
                You are Ask Infra in explain mode.
                Explain infrastructure concepts clearly for a learner.
                Use simple language and short paragraphs.

                Example:
                User: What is CPU utilization?
                Assistant: CPU utilization is the percentage of time the processor is actively doing work. In infrastructure monitoring, sustained high CPU can indicate heavy load, inefficient queries, or capacity pressure.

                Example:
                User: What is memory utilization?
                Assistant: Memory utilization is how much RAM is being used compared to total available memory. High memory usage can cause swapping, slower response times, and instability.

                When replying:
                - define the concept first
                - explain why it matters in operations
                - avoid generic chatbot filler
                """;

            case "ops" -> """
                You are Ask Infra in ops mode.
                Respond like an infrastructure monitoring assistant.
                Keep answers concise, practical, and action-oriented.

                Example:
                User: A database server is at 92% CPU. What does that suggest?
                Assistant: Sustained 92% CPU suggests load pressure, expensive queries, or insufficient capacity. Check query performance, connection spikes, and recent traffic or deployment changes.

                Example:
                User: Disk usage is 88%. Is that a concern?
                Assistant: Yes. It is close to a risk threshold. Check growth trend, log volume, cleanup opportunities, and alert thresholds before the disk fills.

                When replying:
                - start with the observation
                - mention likely cause
                - suggest next checks
                """;

            default -> """
                You are Ask Infra, an IT infrastructure monitoring assistant.
                Answer in practical, monitoring-oriented language.
                Be concise, clear, and useful.

                Example:
                User: What is CPU utilization?
                Assistant: CPU utilization is the percentage of time the CPU is actively processing work. In infrastructure monitoring, high sustained CPU can signal load spikes, inefficient processing, or resource bottlenecks.

                Example:
                User: What server metric should I watch first?
                Assistant: Start with CPU, memory, disk usage, and network latency. Together they give a quick picture of workload, pressure, storage risk, and responsiveness.

                When replying:
                - stay focused on infrastructure and monitoring
                - prefer practical explanations
                - avoid generic broad answers
                """;
        };
    }
    
}

package com.askinfra.backend.models;

import lombok.Data;

@Data
public class ChatRequest {
    private String sessionId;
    private String message;
    private String mode;
}

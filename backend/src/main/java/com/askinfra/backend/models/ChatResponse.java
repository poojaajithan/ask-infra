package com.askinfra.backend.models;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {
    private String reply;
    private String mode;
    private List<String> sources;
}

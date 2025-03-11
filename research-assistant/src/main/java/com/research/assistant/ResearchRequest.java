package com.research.assistant;

public class ResearchRequest {
    private String content;
    private  String operations;

    public void setContent(String content) {
        this.content = content;
    }

    public void setOperations(String operations) {
        this.operations = operations;
    }

    public String getContent() {
        return content;
    }

    public String getOperations() {
        return operations;
    }
}

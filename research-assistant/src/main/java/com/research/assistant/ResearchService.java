package com.research.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ResearchService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    public ResearchService(WebClient.Builder webClient, ObjectMapper objectMapper) {
        this.webClient = webClient.build();
        this.objectMapper = objectMapper;
    }


    public String processContent(ResearchRequest researchRequest) {


        //build prompt
        String prompt = buildPrompt(researchRequest);
        // build AI model
        Map<String, Object> requiredContent = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .bodyValue(requiredContent)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        //parse response
        //return
        return extractTextFromResponse(response);
    }

    //Method to parse response
    private String extractTextFromResponse(String response) {
        try{
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
            if(geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()){
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if (firstCandidate.getContent()!=null &&
                    firstCandidate.getContent().getParts() !=null &&
                        !firstCandidate.getContent().getParts().isEmpty()){
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }
        } catch(Exception e){
            return "Error parsing"+ e.getMessage();
        }
        return "No valid response found";
    }

    private String buildPrompt(ResearchRequest researchRequest){
        StringBuilder prompt = new StringBuilder();
        switch (researchRequest.getOperations()) {
            case "summarize" ->
                    prompt.append("Provide a clear and concise summary of the following text in a few sentences:\n\n");
            case "suggest" ->
                    prompt.append("Provide other link with similar content. Content should be match upto 80%.\n\n");
            default -> throw new IllegalArgumentException("Unknown Operation: " + researchRequest.getOperations());
        }
        prompt.append(researchRequest.getContent());
        return prompt.toString();
    }
}


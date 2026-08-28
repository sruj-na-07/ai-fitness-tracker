//package com.fitness.aiservice.service;
//
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.Map;
//
//@Service
//public class GroqService {
//
//    private final WebClient webClient;
//
//
//    @Value("${groq.api.url}")
//    private String groqUrl;
//    @PostConstruct
//    public void testUrl() {
//        System.out.println("FINAL GROQ URL = " + groqUrl);
//    }
//
//    @Value("${groq.api.key}")
//    private String groqKey;
//
//    @Value("${groq.model}")
//    private String model;
//
//
//    public GroqService(WebClient.Builder webClientBuilder){
//        this.webClient = webClientBuilder.build();
//    }
//
//
//    public String getAnswer(String question){
//
//        System.out.println("Groq URL: " + groqUrl);
//        System.out.println("Groq Model: " + model);
//        System.out.println("Groq Key loaded: "
//                + groqKey.substring(0,5) + "******");
//
//
//        Map<String,Object> requestBody = Map.of(
//                "model", model,
//                "messages", new Object[]{
//                        Map.of(
//                                "role","user",
//                                "content",question
//                        )
//                }
//        );
//
//
//        return webClient.post()
//                .uri(groqUrl)
//                .header(
//                        "Authorization",
//                        "Bearer " + groqKey
//                )
//                .header(
//                        "Content-Type",
//                        "application/json"
//                )
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//    }
//}

package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.Map;

@Service
public class GroqService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api.url}")
    private String groqUrl;

    @Value("${groq.api.key}")
    private String groqKey;

    @Value("${groq.model}")
    private String model;

    @PostConstruct
    public void testUrl() {
        System.out.println("FINAL GROQ URL = " + groqUrl);
    }

    public GroqService() {
        // Use standalone WebClient.create() so Spring Cloud Eureka LoadBalancer does not intercept external Groq calls
        this.webClient = WebClient.create();
    }

    public String getAnswer(String question) {
        System.out.println("Groq URL: " + groqUrl);
        System.out.println("Groq Model: " + model);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", new Object[]{
                        Map.of(
                                "role", "user",
                                "content", question
                        )
                }
        );

        String rawResponse = webClient.post()
                .uri(URI.create(groqUrl))
                .header("Authorization", "Bearer " + groqKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractContentFromResponse(rawResponse);
    }

    private String extractContentFromResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // Clean markdown block wrappers (e.g. ```json ... ```)
            if (content != null) {
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
            }
            return content;
        } catch (Exception e) {
            System.err.println("Error parsing Groq response: " + e.getMessage());
            return rawResponse;
        }
    }
}

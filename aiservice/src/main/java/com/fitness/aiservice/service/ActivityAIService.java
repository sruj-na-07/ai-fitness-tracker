package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    private final GroqService groqService;
    private final RecommendationRepository recommendationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = groqService.getAnswer(prompt);
        log.info("RESPONSE FROM AI: {}", aiResponse);

        // Save AI recommendation to MongoDB
        saveRecommendationToDatabase(activity, aiResponse);

        return aiResponse;
    }

    private void saveRecommendationToDatabase(Activity activity, String aiResponse) {
        try {
            JsonNode root = objectMapper.readTree(aiResponse);

            // Extract overall analysis text
            String overallAnalysis = root.path("analysis").path("overall").asText("Analysis generated successfully.");

            // Extract improvements
            List<String> improvementsList = new ArrayList<>();
            JsonNode improvementsNode = root.path("improvements");
            if (improvementsNode.isArray()) {
                for (JsonNode item : improvementsNode) {
                    String area = item.path("area").asText();
                    String rec = item.path("recommendation").asText();
                    improvementsList.add(area + ": " + rec);
                }
            }

            // Extract suggestions
            List<String> suggestionsList = new ArrayList<>();
            JsonNode suggestionNode = root.path("suggestion");
            if (suggestionNode.isArray()) {
                for (JsonNode item : suggestionNode) {
                    String workout = item.path("workout").asText();
                    String desc = item.path("description").asText();
                    suggestionsList.add(workout + ": " + desc);
                }
            }

            // Extract safety guidelines
            List<String> safetyList = new ArrayList<>();
            JsonNode safetyNode = root.path("safety");
            if (safetyNode.isArray()) {
                for (JsonNode item : safetyNode) {
                    safetyList.add(item.asText());
                }
            }

            // Build Recommendation object
            Recommendation recommendation = Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendation(overallAnalysis)
                    .improvements(improvementsList)
                    .suggestions(suggestionsList)
                    .safety(safetyList)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Save into MongoDB
            Recommendation saved = recommendationRepository.save(recommendation);
            log.info("Successfully saved recommendation to DB with ID: {}", saved.getId());

        } catch (Exception e) {
            log.error("Failed to parse or save recommendation for activity {}: {}", activity.getId(), e.getMessage());
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
                        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format.
                        IMPORTANT: Return ONLY valid JSON, do NOT wrap in markdown code blocks like ```json or ```.
                        
                        {
                            "analysis":{
                            "overall": "Overall analysis here",
                            "pace": "Pace analysis here",
                            "heartRate": "Heart rate analysis here",
                            "caloriesBurned": "Calories analysis here"
                            },
                            "improvements": [
                                {
                                    "area": "Area name",
                                    "recommendation": "Detailed recommendation"
                                }
                            ],
                            "suggestion": [
                                {
                                    "workout": "Workout name",
                                    "description": "Detailed workout description"
                                }
                            ],
                            "safety": [
                                "Safety point 1",
                                "Safety point 2"
                            ]
                        } 
                        
                        Analyze this activity:
                        Activity Type: %s
                        Duration: %d minutes
                        Calories Burned: %d
                        Additional Metrics: %s
                        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}

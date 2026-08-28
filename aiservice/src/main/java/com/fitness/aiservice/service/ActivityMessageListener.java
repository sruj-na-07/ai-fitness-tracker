package com.fitness.aiservice.service;


import com.fitness.aiservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

//These is a listner that will listen to the messages published on the queue
@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAIService aiService;

    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity){
//        log.info("Received activity for processing: {}",activity.getId());
//        log.info("Generated Recommendation: {}",aiService.generateRecommendation(activity));
        log.info("Received activity for processing: {}", activity.getId());
        try {
            String recommendation = aiService.generateRecommendation(activity);
            log.info("Generated Recommendation: {}", recommendation);
        } catch (Exception e) {
            log.error("Failed to process activity ID {}: {}", activity.getId(), e.getMessage());
        }
    }
}

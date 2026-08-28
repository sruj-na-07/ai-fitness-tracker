package com.fitness.activityservice.controller;


import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.service.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor //tells spring to automatically inject this dependency activityService else we get null pointer exception
//when we use activityService
public class ActivityController {


    private ActivityService activityService;

    @PostMapping()
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request){
        //this api allows us to track the activity
          return ResponseEntity.ok(activityService.trackActivity(request));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@RequestHeader("X-User-ID") String userId){
        //this api returns the activities of a specific user , we use
        // list coz the activities can be more than one and the response is for single activity
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }

    @GetMapping("/{activityId}")
    //returns the single activity by its Id
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable String activityId){
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }

}

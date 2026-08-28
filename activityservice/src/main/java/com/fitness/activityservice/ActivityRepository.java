package com.fitness.activityservice;

import com.fitness.activityservice.model.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends MongoRepository<Activity,String> {

    List<Activity> findByUserId(String userId);
    //the hibernate or jpa looks at this method findByUserId
    // and generates the query by itself and returns list of activities
}

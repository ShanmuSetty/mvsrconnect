package com.mvsr.mvsrconnect.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ToxicityService {

    @Value("${moderation.url:http://localhost:5001}")
    private String moderationUrl;

    public boolean isToxic(String text){

        try {

            RestTemplate rest = new RestTemplate();

            Map<String,String> req = Map.of(
                    "text", text
            );

            Map response = rest.postForObject(
                    moderationUrl + "/check",
                    req,
                    Map.class
            );

            double score = (double) response.get("toxicity");

            return score > 0.7;

        } catch (Exception e){

            System.out.println("Toxicity check failed: " + e.getMessage());

            return false; // fail safe
        }
    }
}
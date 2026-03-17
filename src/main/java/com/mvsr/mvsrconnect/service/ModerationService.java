package com.mvsr.mvsrconnect.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ModerationService {

    private final RestTemplate rest = new RestTemplate();

    public boolean isTextToxic(String text, String context){

        Map<String,String> req = Map.of(
                "text", text,
                "context", context == null ? "" : context
        );

        Map res = rest.postForObject(
                "http://localhost:5001/check_text",
                req,
                Map.class
        );

        return (boolean) res.get("toxic");
    }
    public boolean isTextToxic(String text){
        return isTextToxic(text, "");
    }


    public boolean isImageUnsafe(String url){

        Map<String,String> req = Map.of("url", url);

        Map res = rest.postForObject(
                "http://localhost:5001/check_image",
                req,
                Map.class
        );

        return (boolean) res.get("unsafe");
    }


    public boolean isVideoUnsafe(String url){

        Map<String,String> req = Map.of("url", url);

        Map res = rest.postForObject(
                "http://localhost:5001/check_video",
                req,
                Map.class
        );

        return (boolean) res.get("unsafe");
    }

}
package com.mvsr.mvsrconnect.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaUploader {

    private final Cloudinary cloudinary;

    public MediaUploader(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @PostMapping("/upload")
    public Map uploadMedia(@RequestParam("file") MultipartFile file) throws Exception {

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto")
        );

        return Map.of(
                "url", uploadResult.get("secure_url"),
                "publicId", uploadResult.get("public_id")
        );
    }
}
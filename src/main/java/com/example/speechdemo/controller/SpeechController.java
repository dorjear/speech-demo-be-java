package com.example.speechdemo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/Voice")
public class SpeechController {

    @Value("${azure.speech.key}")
    private String azureSpeechKey;

    @Value("${azure.region}")
    private String azureRegion;

    @GetMapping("/get-speech-token")
    public ResponseEntity<?> getSpeechToken() {
        if ("your-secret-key".equals(azureSpeechKey) || "eastus".equals(azureRegion)) {
            return ResponseEntity.badRequest().body("You forgot to add your speech key or region to the application.properties file.");
        }

        try {
            // Simulate token generation (replace with actual Azure SDK logic)
            String token = "mock-token"; // Replace with actual token generation logic
            return ResponseEntity.ok().body("{\"token\": \"" + token + "\", \"region\": \"" + azureRegion + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("There was an error authorizing your speech key.");
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded.");
        }

        try {
            // Save the uploaded file
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String webmFileName = file.getOriginalFilename();
            Path webmFilePath = uploadPath.resolve(webmFileName);
            Files.write(webmFilePath, file.getBytes());

            // Simulate conversion to WAV (replace with actual logic)
            String wavFileName = webmFileName.replace(".webm", ".wav");
            Path wavFilePath = uploadPath.resolve(wavFileName);
            Files.write(wavFilePath, "mock-wav-content".getBytes()); // Replace with actual conversion logic

            return ResponseEntity.ok().body("File uploaded and converted to WAV: " + wavFileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing audio file.");
        }
    }

    @PostMapping("/translate")
    public ResponseEntity<?> translateFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded.");
        }

        try {
            // Save the uploaded file
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String webmFileName = file.getOriginalFilename();
            Path webmFilePath = uploadPath.resolve(webmFileName);
            Files.write(webmFilePath, file.getBytes());

            // Simulate conversion to WAV and translation (replace with actual logic)
            String wavFileName = webmFileName.replace(".webm", ".wav");
            Path wavFilePath = uploadPath.resolve(wavFileName);
            Files.write(wavFilePath, "mock-wav-content".getBytes()); // Replace with actual conversion logic

            String translatedText = "mock-translated-text"; // Replace with actual translation logic
            return ResponseEntity.ok().body("{\"DisplayText\": \"" + translatedText + "\"}");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing audio file.");
        }
    }
}
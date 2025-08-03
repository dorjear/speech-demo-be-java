package com.example.speechdemo.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

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
            HttpHeaders headers = new HttpHeaders();
            headers.set("Ocp-Apim-Subscription-Key", azureSpeechKey);
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> entity = new HttpEntity<>(null, headers);

            String url = "https://" + azureRegion + ".api.cognitive.microsoft.com/sts/v1.0/issueToken";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            String token = response.getBody();
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

        Path uploadPath = Paths.get("uploads");
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save the uploaded .webm file
            String webmFileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
            Path webmFilePath = uploadPath.resolve(webmFileName);
            Files.write(webmFilePath, file.getBytes());

            // Convert .webm to .wav using ffmpeg
            String wavFileName = webmFileName.replace(".webm", ".wav");
            Path wavFilePath = uploadPath.resolve(wavFileName);
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y", "-i", webmFilePath.toString(), wavFilePath.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(500).body("Error converting file to WAV.");
            }

            // Use Azure Speech SDK to transcribe the .wav file
            String recognizedText = recognizeSpeech(wavFilePath.toString());
            if (recognizedText == null) {
                return ResponseEntity.status(500).body("Could not transcribe the audio file.");
            }

            // Clean up files
            Files.deleteIfExists(webmFilePath);
            Files.deleteIfExists(wavFilePath);

            return ResponseEntity.ok().body("{\"DisplayText\": \"" + recognizedText + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing audio file.");
        }
    }

    // Helper method to use Azure Speech SDK
    private String recognizeSpeech(String wavFilePath) {
        try {
            com.microsoft.cognitiveservices.speech.SpeechConfig speechConfig =
                    com.microsoft.cognitiveservices.speech.SpeechConfig.fromSubscription(azureSpeechKey, azureRegion);
            speechConfig.setSpeechRecognitionLanguage("en-US");
            com.microsoft.cognitiveservices.speech.audio.AudioConfig audioConfig =
                    com.microsoft.cognitiveservices.speech.audio.AudioConfig.fromWavFileInput(wavFilePath);
            com.microsoft.cognitiveservices.speech.SpeechRecognizer recognizer =
                    new com.microsoft.cognitiveservices.speech.SpeechRecognizer(speechConfig, audioConfig);
            com.microsoft.cognitiveservices.speech.SpeechRecognitionResult result = recognizer.recognizeOnceAsync().get();
            recognizer.close();
            if (result.getReason() == com.microsoft.cognitiveservices.speech.ResultReason.RecognizedSpeech) {
                return result.getText();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
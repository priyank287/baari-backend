package com.baari.app.notification;

import com.baari.service.entity.enums.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * WhatsApp delivery via Interakt (https://app.interakt.ai).
 *
 * Setup required before use:
 * 1. Create a free account at app.interakt.ai
 * 2. Connect your WhatsApp Business number
 * 3. Create a template named exactly as configured in notification.interakt.template-name
 *    Template body: {{1}}   (single variable — the full message text)
 *    Category: UTILITY
 * 4. Get your API key from Settings → Developer → API Keys
 * 5. Set INTERAKT_API_KEY in .env
 */
@Slf4j
public class InteraktChannel implements NotificationChannel {

    private static final String API_URL = "https://api.interakt.ai/v1/public/message/";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String countryCode;
    private final String templateName;
    private final String languageCode;

    public InteraktChannel(RestTemplate restTemplate, String apiKey,
                           String countryCode, String templateName, String languageCode) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.countryCode = countryCode;
        this.templateName = templateName;
        this.languageCode = languageCode;
    }

    @Override
    public boolean send(String mobileNumber, String message, MessageType messageType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + apiKey);

            Map<String, Object> body = Map.of(
                    "countryCode", countryCode,
                    "phoneNumber", mobileNumber,
                    "callbackData", messageType.name(),
                    "type", "Template",
                    "template", Map.of(
                            "name", templateName,
                            "languageCode", languageCode,
                            "bodyValues", List.of(message)
                    )
            );

            log.info("[Interakt] Sending [{}] to +{}{}", messageType, countryCode, mobileNumber);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    API_URL, new HttpEntity<>(body, headers), String.class);

            log.info("[Interakt] Response [{}] | HTTP {} | Body: {}",
                    messageType, response.getStatusCode(), response.getBody());

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("[Interakt] Error [{}] to {}: {}", messageType, mobileNumber, e.getMessage());
            return false;
        }
    }
}

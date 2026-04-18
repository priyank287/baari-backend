package com.baari.app.notification;

import com.baari.service.entity.enums.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class Fast2SmsChannel implements NotificationChannel {

    private static final String URL = "https://www.fast2sms.com/dev/bulkV2";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public Fast2SmsChannel(RestTemplate restTemplate, String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    @Override
    public boolean send(String mobileNumber, String message, MessageType messageType) {
        try {
            String url = UriComponentsBuilder.fromUri(java.net.URI.create(URL))
                    .queryParam("authorization", apiKey)
                    .queryParam("route", "q")
                    .queryParam("message", message)
                    .queryParam("language", "english")
                    .queryParam("numbers", mobileNumber)
                    .toUriString();

            log.info("[Fast2SMS] Sending [{}] to {} | URL (masked): {}",
                    messageType, mobileNumber,
                    url.replaceAll("authorization=[^&]+", "authorization=***"));

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

            log.info("[Fast2SMS] Response [{}] | HTTP {} | Body: {}",
                    messageType, response.getStatusCode(), response.getBody());

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("[Fast2SMS] Error [{}] to {}: {}", messageType, mobileNumber, e.getMessage());
            return false;
        }
    }
}

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
 * WhatsApp delivery via WATI (https://app.wati.io).
 *
 * Setup required before use:
 * 1. Create a free trial account at app.wati.io
 * 2. Connect your WhatsApp Business number (or use the sandbox for testing)
 * 3. Create a template named exactly as configured in notification.wati.template-name
 *    Template body: {{1}}   (single variable — the full message text)
 *    Category: UTILITY
 * 4. Get your API endpoint from WATI dashboard (e.g. https://live-mt-server.wati.io/xxxxx)
 * 5. Get your API token from Settings → API
 * 6. Set WATI_API_ENDPOINT and WATI_API_TOKEN in .env
 *
 * Sandbox testing:
 * - WATI sandbox lets you send to registered test numbers without template approval
 * - Register test numbers under Sandbox → Test Numbers in the WATI dashboard
 */
@Slf4j
public class WatiChannel implements NotificationChannel {

    private final RestTemplate restTemplate;
    private final String apiEndpoint;   // e.g. https://live-mt-server.wati.io/xxxxx
    private final String apiToken;
    private final String templateName;
    private final String broadcastName;

    public WatiChannel(RestTemplate restTemplate, String apiEndpoint, String apiToken,
                       String templateName, String broadcastName) {
        this.restTemplate = restTemplate;
        this.apiEndpoint = apiEndpoint.replaceAll("/$", ""); // strip trailing slash
        this.apiToken = apiToken;
        this.templateName = templateName;
        this.broadcastName = broadcastName;
    }

    @Override
    public boolean send(String mobileNumber, String message, MessageType messageType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);

            // WATI expects number with country code, no + sign (e.g. 919876543210)
            String fullNumber = mobileNumber.startsWith("91") ? mobileNumber : "91" + mobileNumber;

            Map<String, Object> body = Map.of(
                    "template_name", templateName,
                    "broadcast_name", broadcastName,
                    "parameters", List.of(
                            Map.of("name", "1", "value", message)
                    )
            );

            String url = apiEndpoint + "/api/v1/sendTemplateMessage?whatsappNumber=" + fullNumber;

            log.info("[WATI] Sending [{}] to {}", messageType, fullNumber);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(body, headers), String.class);

            log.info("[WATI] Response [{}] | HTTP {} | Body: {}",
                    messageType, response.getStatusCode(), response.getBody());

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("[WATI] Error [{}] to {}: {}", messageType, mobileNumber, e.getMessage());
            return false;
        }
    }
}

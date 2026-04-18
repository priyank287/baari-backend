package com.baari.app.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class NotificationChannelConfig {

    @Value("${notification.channel:fast2sms}")
    private String channel;

    // Fast2SMS
    @Value("${notification.fast2sms.api-key:}")
    private String fast2smsApiKey;

    // Interakt
    @Value("${notification.interakt.api-key:}")
    private String interaktApiKey;

    @Value("${notification.interakt.country-code:91}")
    private String interaktCountryCode;

    @Value("${notification.interakt.template-name:baari_notification}")
    private String interaktTemplateName;

    @Value("${notification.interakt.language-code:en}")
    private String interaktLanguageCode;

    // WATI
    @Value("${notification.wati.api-endpoint:}")
    private String watiApiEndpoint;

    @Value("${notification.wati.api-token:}")
    private String watiApiToken;

    @Value("${notification.wati.template-name:baari_notification}")
    private String watiTemplateName;

    @Value("${notification.wati.broadcast-name:baari_queue}")
    private String watiBroadcastName;

    @Bean
    public NotificationChannel notificationChannel(RestTemplate restTemplate) {
        return switch (channel.toLowerCase()) {
            case "interakt" -> {
                log.info("Notification channel: Interakt (WhatsApp)");
                yield new InteraktChannel(restTemplate, interaktApiKey,
                        interaktCountryCode, interaktTemplateName, interaktLanguageCode);
            }
            case "wati" -> {
                log.info("Notification channel: WATI (WhatsApp)");
                yield new WatiChannel(restTemplate, watiApiEndpoint, watiApiToken,
                        watiTemplateName, watiBroadcastName);
            }
            default -> {
                log.info("Notification channel: Fast2SMS (SMS)");
                yield new Fast2SmsChannel(restTemplate, fast2smsApiKey);
            }
        };
    }
}

package com.baari.app.notification;

import com.baari.service.entity.enums.MessageType;

/**
 * Abstraction over SMS and WhatsApp delivery providers.
 * Implementations: Fast2SmsChannel, InteraktChannel, WatiChannel.
 * Active channel is selected via: notification.channel in application.yml
 */
public interface NotificationChannel {

    /**
     * Send a message to a mobile number.
     *
     * @param mobileNumber  recipient's number (digits only, no country code prefix needed)
     * @param message       full message text
     * @param messageType   used for logging context
     * @return true if the provider accepted the message
     */
    boolean send(String mobileNumber, String message, MessageType messageType);
}

package com.nihap.lostlink;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Message implements Serializable {
    private String messageId;
    private String senderId;
    private String senderName;
    private String message;
    private Timestamp timestamp;
    private String messageType; // "text", "image", "location"
    private boolean isRead;

    public Message() {}

    public Message(String senderId, String senderName, String message, String messageType) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.messageType = messageType;
        this.timestamp = Timestamp.now();
        this.isRead = false;
    }

    // Getters
    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getMessage() { return message; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getMessageType() { return messageType; }
    public boolean isRead() { return isRead; }

    // Setters
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public void setRead(boolean read) { isRead = read; }
}

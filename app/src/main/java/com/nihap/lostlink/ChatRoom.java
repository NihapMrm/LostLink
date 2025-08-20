package com.nihap.lostlink;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.List;

public class ChatRoom implements Serializable {
    private String chatRoomId;
    private List<String> participants;
    private String reportId;
    private String reportTitle;
    private String reportType;
    private String reportImageUrl;
    private Timestamp createdAt;
    private String lastMessage;
    private Timestamp lastMessageTime;
    private boolean isActive;
    private String otherUserName;
    private String otherUserId;
    private int unreadCount;
    private String lastSenderId;

    public ChatRoom() {}

    public ChatRoom(String chatRoomId, List<String> participants, String reportId,
                   String reportTitle, String reportType, String reportImageUrl) {
        this.chatRoomId = chatRoomId;
        this.participants = participants;
        this.reportId = reportId;
        this.reportTitle = reportTitle;
        this.reportType = reportType;
        this.reportImageUrl = reportImageUrl;
        this.createdAt = Timestamp.now();
        this.isActive = true;
        this.lastMessage = "";
        this.unreadCount = 0;
    }

    // Getters
    public String getChatRoomId() { return chatRoomId; }
    public List<String> getParticipants() { return participants; }
    public String getReportId() { return reportId; }
    public String getReportTitle() { return reportTitle; }
    public String getReportType() { return reportType; }
    public String getReportImageUrl() { return reportImageUrl; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getLastMessage() { return lastMessage; }
    public Timestamp getLastMessageTime() { return lastMessageTime; }
    public boolean isActive() { return isActive; }
    public String getOtherUserName() { return otherUserName; }
    public String getOtherUserId() { return otherUserId; }
    public int getUnreadCount() { return unreadCount; }
    public String getLastSenderId() { return lastSenderId; }

    // Setters
    public void setChatRoomId(String chatRoomId) { this.chatRoomId = chatRoomId; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public void setReportImageUrl(String reportImageUrl) { this.reportImageUrl = reportImageUrl; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageTime(Timestamp lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public void setActive(boolean active) { isActive = active; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public void setLastSenderId(String lastSenderId) { this.lastSenderId = lastSenderId; }
}

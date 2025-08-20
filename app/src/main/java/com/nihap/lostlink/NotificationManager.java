package com.nihap.lostlink;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationManager {
    private static final String TAG = "NotificationManager";
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private NotificationUpdateListener listener;
    private ListenerRegistration chatRoomsListener;

    public interface NotificationUpdateListener {
        void onNotificationCountChanged(int count);
    }

    public NotificationManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        startListeningForUnreadMessages();
    }

    public void setNotificationUpdateListener(NotificationUpdateListener listener) {
        this.listener = listener;
    }

    private void startListeningForUnreadMessages() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (currentUserId == null) return;

        chatRoomsListener = db.collection("chatRooms")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        AtomicInteger totalUnread = new AtomicInteger(0);

                        for (QueryDocumentSnapshot doc : value) {
                            ChatRoom chatRoom = doc.toObject(ChatRoom.class);
                            String lastSenderId = chatRoom.getLastSenderId();

                            // Only count as unread if the last message was not sent by current user
                            if (lastSenderId != null && !lastSenderId.equals(currentUserId)) {
                                int unreadCount = chatRoom.getUnreadCount();
                                if (unreadCount > 0) {
                                    totalUnread.addAndGet(unreadCount);
                                }
                            }
                        }

                        if (listener != null) {
                            listener.onNotificationCountChanged(totalUnread.get());
                        }
                    }
                });
    }

    public void updateNotificationBadge() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (currentUserId == null) return;

        db.collection("chatRooms")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUnread = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ChatRoom chatRoom = doc.toObject(ChatRoom.class);
                        String lastSenderId = chatRoom.getLastSenderId();

                        // Only count as unread if the last message was not sent by current user
                        if (lastSenderId != null && !lastSenderId.equals(currentUserId)) {
                            int unreadCount = chatRoom.getUnreadCount();
                            if (unreadCount > 0) {
                                totalUnread += unreadCount;
                            }
                        }
                    }

                    if (listener != null) {
                        listener.onNotificationCountChanged(totalUnread);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error getting chat rooms", e));
    }

    public void markChatRoomAsRead(String chatRoomId) {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (currentUserId == null) return;

        // Reset unread count for this chat room
        db.collection("chatRooms").document(chatRoomId)
                .update("unreadCount", 0)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat room marked as read: " + chatRoomId);
                    updateNotificationBadge(); // Refresh badge count
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error marking chat room as read", e));
    }

    public void destroy() {
        if (chatRoomsListener != null) {
            chatRoomsListener.remove();
        }
    }
}

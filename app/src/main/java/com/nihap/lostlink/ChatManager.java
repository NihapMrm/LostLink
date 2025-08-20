package com.nihap.lostlink;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatManager {
    private static final String TAG = "ChatManager";
    private static final String CHAT_ROOMS_COLLECTION = "chatRooms";
    private static final String MESSAGES_COLLECTION = "messages";
    private static final String USERS_COLLECTION = "users";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public interface ChatRoomCallback {
        void onSuccess(String chatRoomId);
        void onError(Exception e);
    }

    public interface ChatRoomsCallback {
        void onSuccess(List<ChatRoom> chatRooms);
        void onError(Exception e);
    }

    public interface MessagesCallback {
        void onMessagesUpdated(List<Message> messages);
        void onError(Exception e);
    }

    public interface MessageSentCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public ChatManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void createOrGetChatRoom(String reportId, String reportTitle, String reportType,
                                   String reportImageUrl, String reportOwnerId, String reportOwnerName,
                                   ChatRoomCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        String currentUserId = currentUser.getUid();
        String currentUserName = currentUser.getDisplayName();

        // Create participants list (sorted for consistent chat room ID)
        List<String> participants = Arrays.asList(currentUserId, reportOwnerId);
        participants.sort(String::compareTo);

        // Generate chat room ID based on participants and report
        String chatRoomId = reportId + "_" + participants.get(0) + "_" + participants.get(1);

        // Check if chat room already exists
        db.collection(CHAT_ROOMS_COLLECTION)
            .document(chatRoomId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Chat room exists, return its ID
                    callback.onSuccess(chatRoomId);
                } else {
                    // Create new chat room
                    ChatRoom chatRoom = new ChatRoom(chatRoomId, participants, reportId,
                                                   reportTitle, reportType, reportImageUrl);

                    db.collection(CHAT_ROOMS_COLLECTION)
                        .document(chatRoomId)
                        .set(chatRoom)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(chatRoomId))
                        .addOnFailureListener(callback::onError);
                }
            })
            .addOnFailureListener(callback::onError);
    }

    public void getUserChatRooms(ChatRoomsCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated");
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        String currentUserId = currentUser.getUid();
        Log.d(TAG, "Fetching chat rooms for user: " + currentUserId);

        db.collection(CHAT_ROOMS_COLLECTION)
            .whereArrayContains("participants", currentUserId)
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " chat rooms in database");
                List<ChatRoom> chatRooms = new java.util.ArrayList<>();

                if (queryDocumentSnapshots.isEmpty()) {
                    Log.d(TAG, "No chat rooms found, returning empty list");
                    callback.onSuccess(chatRooms);
                    return;
                }

                int[] processedCount = {0}; // Use array to make it mutable in lambda

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Log.d(TAG, "Processing chat room: " + document.getId());
                    ChatRoom chatRoom = document.toObject(ChatRoom.class);
                    chatRoom.setChatRoomId(document.getId()); // Ensure chat room ID is set

                    // Set other user info
                    List<String> participants = chatRoom.getParticipants();
                    if (participants != null && participants.size() >= 2) {
                        String otherUserId = participants.get(0).equals(currentUserId) ?
                                            participants.get(1) : participants.get(0);
                        chatRoom.setOtherUserId(otherUserId);
                        Log.d(TAG, "Other user ID: " + otherUserId);

                        // Get other user's name from auth or use fallback
                        getUserName(otherUserId, userName -> {
                            Log.d(TAG, "Got username: " + userName + " for user: " + otherUserId);
                            chatRoom.setOtherUserName(userName);
                            chatRooms.add(chatRoom);
                            processedCount[0]++;

                            if (processedCount[0] == queryDocumentSnapshots.size()) {
                                Log.d(TAG, "All chat rooms processed, returning " + chatRooms.size() + " rooms");
                                // Sort by last message time or creation time
                                chatRooms.sort((a, b) -> {
                                    Timestamp aTime = a.getLastMessageTime() != null ?
                                                     a.getLastMessageTime() : a.getCreatedAt();
                                    Timestamp bTime = b.getLastMessageTime() != null ?
                                                     b.getLastMessageTime() : b.getCreatedAt();

                                    if (aTime == null && bTime == null) return 0;
                                    if (aTime == null) return 1;
                                    if (bTime == null) return -1;

                                    return bTime.compareTo(aTime); // Descending order
                                });

                                callback.onSuccess(chatRooms);
                            }
                        });
                    } else {
                        Log.w(TAG, "Skipping malformed chat room with invalid participants");
                        // Skip malformed chat room
                        processedCount[0]++;
                        if (processedCount[0] == queryDocumentSnapshots.size()) {
                            callback.onSuccess(chatRooms);
                        }
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching chat rooms", e);
                callback.onError(e);
            });
    }

    public ListenerRegistration listenToMessages(String chatRoomId, MessagesCallback callback) {
        return db.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .collection(MESSAGES_COLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Message> messages = new java.util.ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Message message = document.toObject(Message.class);
                            message.setMessageId(document.getId());
                            messages.add(message);
                        }
                        callback.onMessagesUpdated(messages);
                    }
                });
    }

    public void sendMessage(String chatRoomId, String messageText, MessageSentCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        String currentUserId = currentUser.getUid();
        String currentUserName = currentUser.getDisplayName() != null ?
                                currentUser.getDisplayName() : "Anonymous";

        Message message = new Message(currentUserId, currentUserName, messageText, "text");

        // Add message to subcollection
        db.collection(CHAT_ROOMS_COLLECTION)
            .document(chatRoomId)
            .collection(MESSAGES_COLLECTION)
            .add(message)
            .addOnSuccessListener(documentReference -> {
                // Update chat room's last message
                Map<String, Object> updates = new HashMap<>();
                updates.put("lastMessage", messageText);
                updates.put("lastMessageTime", Timestamp.now());

                db.collection(CHAT_ROOMS_COLLECTION)
                    .document(chatRoomId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(callback::onError);
            })
            .addOnFailureListener(callback::onError);
    }

    public void sendMessage(String chatRoomId, String message, String messageType, MessageSentCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        String senderId = currentUser.getUid();
        String senderName = currentUser.getDisplayName() != null ?
                           currentUser.getDisplayName() : "Unknown User";

        Message messageObj = new Message(senderId, senderName, message, messageType);

        // Add message to messages subcollection
        db.collection(CHAT_ROOMS_COLLECTION)
            .document(chatRoomId)
            .collection(MESSAGES_COLLECTION)
            .add(messageObj)
            .addOnSuccessListener(documentReference -> {
                // Update chat room's last message info and increment unread count
                Map<String, Object> updates = new HashMap<>();
                updates.put("lastMessage", message);
                updates.put("lastMessageTime", Timestamp.now());
                updates.put("lastSenderId", senderId);

                // Get current unread count and increment it
                db.collection(CHAT_ROOMS_COLLECTION)
                    .document(chatRoomId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        int currentUnread = 0;
                        if (doc.exists() && doc.contains("unreadCount")) {
                            currentUnread = doc.getLong("unreadCount").intValue();
                        }
                        updates.put("unreadCount", currentUnread + 1);

                        // Update chat room
                        db.collection(CHAT_ROOMS_COLLECTION)
                            .document(chatRoomId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                // Send notification to other participants
                                sendNotificationToParticipants(chatRoomId, senderName, message);
                                callback.onSuccess();
                            })
                            .addOnFailureListener(callback::onError);
                    })
                    .addOnFailureListener(callback::onError);
            })
            .addOnFailureListener(callback::onError);
    }

    private void sendNotificationToParticipants(String chatRoomId, String senderName, String message) {
        // Get chat room details for notification
        db.collection(CHAT_ROOMS_COLLECTION)
            .document(chatRoomId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    ChatRoom chatRoom = doc.toObject(ChatRoom.class);
                    if (chatRoom != null) {
                        String reportTitle = chatRoom.getReportTitle();

                        // Here you would typically send the notification to FCM server
                        // For now, we'll just log it
                        Log.d(TAG, "Would send notification - Sender: " + senderName +
                                   ", Message: " + message + ", Report: " + reportTitle);
                    }
                }
            });
    }

    private void getUserName(String userId, UserNameCallback callback) {
        // First try to get the user name from the reports collection
        db.collection("reports")
            .whereEqualTo("userId", userId)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    String userName = queryDocumentSnapshots.getDocuments().get(0).getString("userName");
                    if (userName != null && !userName.isEmpty()) {
                        callback.onUserName(userName);
                        return;
                    }
                }

                // If no userName found in reports, try users collection as fallback
                db.collection(USERS_COLLECTION)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name");
                            callback.onUserName(userName != null ? userName : "Anonymous User");
                        } else {
                            callback.onUserName("Anonymous User");
                        }
                    })
                    .addOnFailureListener(e -> callback.onUserName("Anonymous User"));
            })
            .addOnFailureListener(e -> callback.onUserName("Anonymous User"));
    }

    private interface UserNameCallback {
        void onUserName(String userName);
    }
}

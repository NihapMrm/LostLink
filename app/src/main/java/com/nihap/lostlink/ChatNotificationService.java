package com.nihap.lostlink;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatNotificationService extends FirebaseMessagingService {
    private static final String TAG = "ChatNotification";
    private static final String CHANNEL_ID = "chat_notifications";
    private static final String CHANNEL_NAME = "Chat Messages";
    private static final String CHANNEL_DESCRIPTION = "Notifications for new chat messages";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String chatRoomId = remoteMessage.getData().get("chatRoomId");
            String senderName = remoteMessage.getData().get("senderName");
            String message = remoteMessage.getData().get("message");
            String reportTitle = remoteMessage.getData().get("reportTitle");

            if (chatRoomId != null && senderName != null && message != null) {
                showNotification(chatRoomId, senderName, message, reportTitle);
            }
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification("", remoteMessage.getNotification().getTitle(),
                           remoteMessage.getNotification().getBody(), "");
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server
        Log.d(TAG, "sendRegistrationToServer(" + token + ")");
    }

    private void showNotification(String chatRoomId, String senderName, String message, String reportTitle) {
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("openChat", true);
        intent.putExtra("chatRoomId", chatRoomId);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String notificationTitle = senderName;
        String notificationBody = message;

        if (reportTitle != null && !reportTitle.isEmpty()) {
            notificationTitle = senderName + " • " + reportTitle;
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(chatRoomId.hashCode(), notificationBuilder.build());
            }
        } else {
            notificationManager.notify(chatRoomId.hashCode(), notificationBuilder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

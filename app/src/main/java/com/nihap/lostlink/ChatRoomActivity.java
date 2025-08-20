package com.nihap.lostlink;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageView buttonSendMessage;
    private ImageView imageViewBack;
    private ImageView imageViewChatItem;
    private TextView textViewChatItemTitle;
    private TextView textViewChatWithUser;
    private TextView textViewChatReportType;

    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private ChatRoom chatRoom;
    private ChatManager chatManager;
    private ListenerRegistration messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        initializeViews();
        getChatRoomFromIntent();
        setupRecyclerView();
        setupClickListeners();
        loadMessages();

        // Mark chat room as read when opened
        markChatRoomAsRead();
    }

    private void initializeViews() {
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        imageViewBack = findViewById(R.id.imageViewBack);
        imageViewChatItem = findViewById(R.id.imageViewChatItem);
        textViewChatItemTitle = findViewById(R.id.textViewChatItemTitle);
        textViewChatWithUser = findViewById(R.id.textViewChatWithUser);
        textViewChatReportType = findViewById(R.id.textViewChatReportType);

        messages = new ArrayList<>();
        chatManager = new ChatManager();
    }

    private void getChatRoomFromIntent() {
        // Get individual fields from intent instead of the entire ChatRoom object
        String chatRoomId = getIntent().getStringExtra("chatRoomId");
        String reportId = getIntent().getStringExtra("reportId");
        String reportTitle = getIntent().getStringExtra("reportTitle");
        String reportType = getIntent().getStringExtra("reportType");
        String reportImageUrl = getIntent().getStringExtra("reportImageUrl");
        String otherUserName = getIntent().getStringExtra("otherUserName");
        String otherUserId = getIntent().getStringExtra("otherUserId");
        String lastMessage = getIntent().getStringExtra("lastMessage");

        if (chatRoomId == null || reportTitle == null) {
            finish();
            return;
        }

        // Reconstruct ChatRoom object from individual fields
        chatRoom = new ChatRoom();
        chatRoom.setChatRoomId(chatRoomId);
        chatRoom.setReportId(reportId);
        chatRoom.setReportTitle(reportTitle);
        chatRoom.setReportType(reportType);
        chatRoom.setReportImageUrl(reportImageUrl);
        chatRoom.setOtherUserName(otherUserName);
        chatRoom.setOtherUserId(otherUserId);
        chatRoom.setLastMessage(lastMessage);

        setupChatRoomInfo();
    }

    private void setupChatRoomInfo() {
        textViewChatItemTitle.setText(chatRoom.getReportTitle());
        textViewChatWithUser.setText("Chat with " + chatRoom.getOtherUserName());
        textViewChatReportType.setText(chatRoom.getReportType().toUpperCase());

        // Load item image
        if (chatRoom.getReportImageUrl() != null && !chatRoom.getReportImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(chatRoom.getReportImageUrl())
                    .placeholder(R.drawable.default_profile)
                    .into(imageViewChatItem);
        } else {
            imageViewChatItem.setImageResource(R.drawable.default_profile);
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, messages);
        recyclerViewMessages.setAdapter(messageAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
    }

    private void setupClickListeners() {
        imageViewBack.setOnClickListener(v -> finish());

        buttonSendMessage.setOnClickListener(v -> sendMessage());

        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void loadMessages() {
        messagesListener = chatManager.listenToMessages(chatRoom.getChatRoomId(), new ChatManager.MessagesCallback() {
            @Override
            public void onMessagesUpdated(List<Message> newMessages) {
                messages.clear();
                messages.addAll(newMessages);
                messageAdapter.updateMessages(messages);

                if (!messages.isEmpty()) {
                    recyclerViewMessages.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ChatRoomActivity.this, "Error loading messages: " + e.getMessage(),
                             Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        editTextMessage.setText("");

        chatManager.sendMessage(chatRoom.getChatRoomId(), messageText, new ChatManager.MessageSentCallback() {
            @Override
            public void onSuccess() {
                // Message sent successfully
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ChatRoomActivity.this, "Failed to send message: " + e.getMessage(),
                             Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markChatRoomAsRead() {
        if (chatRoom != null && chatRoom.getChatRoomId() != null) {
            NotificationManager notificationManager = new NotificationManager(this);
            notificationManager.markChatRoomAsRead(chatRoom.getChatRoomId());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}

package com.nihap.lostlink;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {
    private List<ChatRoom> chatRooms;
    private Context context;

    public ChatRoomAdapter(Context context, List<ChatRoom> chatRooms) {
        this.context = context;
        this.chatRooms = chatRooms;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);

        // Set report title
        holder.textViewReportTitle.setText(chatRoom.getReportTitle());

        // Set report type
        holder.textViewReportType.setText(chatRoom.getReportType().toUpperCase());

        // Set other user name
        holder.textViewOtherUserName.setText(chatRoom.getOtherUserName());

        // Set last message
        String lastMessage = chatRoom.getLastMessage();
        if (lastMessage == null || lastMessage.isEmpty()) {
            holder.textViewLastMessage.setText("Start a conversation");
        } else {
            holder.textViewLastMessage.setText(lastMessage);
        }

        // Set time
        if (chatRoom.getLastMessageTime() != null) {
            holder.textViewTime.setText(formatTime(chatRoom.getLastMessageTime()));
        } else if (chatRoom.getCreatedAt() != null) {
            holder.textViewTime.setText(formatTime(chatRoom.getCreatedAt()));
        }

        // Show/hide unread indicator
        if (chatRoom.getUnreadCount() > 0) {
            holder.unreadIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.unreadIndicator.setVisibility(View.GONE);
        }

        // Load item image
        if (chatRoom.getReportImageUrl() != null && !chatRoom.getReportImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(chatRoom.getReportImageUrl())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.imageViewItem);
        } else {
            holder.imageViewItem.setImageResource(R.drawable.default_profile);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatRoomActivity.class);
            // Pass individual fields instead of the entire ChatRoom object to avoid serialization issues
            intent.putExtra("chatRoomId", chatRoom.getChatRoomId());
            intent.putExtra("reportId", chatRoom.getReportId());
            intent.putExtra("reportTitle", chatRoom.getReportTitle());
            intent.putExtra("reportType", chatRoom.getReportType());
            intent.putExtra("reportImageUrl", chatRoom.getReportImageUrl());
            intent.putExtra("otherUserName", chatRoom.getOtherUserName());
            intent.putExtra("otherUserId", chatRoom.getOtherUserId());
            intent.putExtra("lastMessage", chatRoom.getLastMessage());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public void updateChatRooms(List<ChatRoom> newChatRooms) {
        this.chatRooms = newChatRooms;
        notifyDataSetChanged();
    }

    private String formatTime(Timestamp timestamp) {
        Date date = timestamp.toDate();
        Date now = new Date();

        // If today, show time only
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

        // Check if same day
        if (android.text.format.DateUtils.isToday(date.getTime())) {
            return timeFormat.format(date);
        } else {
            return dateFormat.format(date);
        }
    }

    static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewItem;
        TextView textViewReportTitle;
        TextView textViewReportType;
        TextView textViewOtherUserName;
        TextView textViewLastMessage;
        TextView textViewTime;
        View unreadIndicator; // Add unread indicator view

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewReportTitle = itemView.findViewById(R.id.textViewReportTitle);
            textViewReportType = itemView.findViewById(R.id.textViewReportType);
            textViewOtherUserName = itemView.findViewById(R.id.textViewOtherUserName);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator); // Initialize unread indicator
        }
    }
}

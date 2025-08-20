package com.nihap.lostlink;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_MESSAGE_SENT = 1;
    private static final int TYPE_MESSAGE_RECEIVED = 2;

    private List<Message> messages;
    private Context context;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return TYPE_MESSAGE_SENT;
        } else {
            return TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MESSAGE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    private String formatTime(Timestamp timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(timestamp.toDate());
    }

    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        TextView textViewTime;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }

        public void bind(Message message) {
            textViewMessage.setText(message.getMessage());
            textViewTime.setText(formatTime(message.getTimestamp()));
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        TextView textViewTime;
        TextView textViewSenderName;
        ImageView imageViewSenderProfile;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
            imageViewSenderProfile = itemView.findViewById(R.id.imageViewSenderProfile);
        }

        public void bind(Message message) {
            textViewMessage.setText(message.getMessage());
            textViewTime.setText(formatTime(message.getTimestamp()));
            textViewSenderName.setText(message.getSenderName());

            // Load sender profile picture
            // For now, we'll use the default profile picture
            // In a full implementation, you'd fetch the user's profile picture from Firestore
            Glide.with(context)
                    .load(R.drawable.default_profile) // You can replace this with actual profile URL
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(imageViewSenderProfile);
        }
    }
}

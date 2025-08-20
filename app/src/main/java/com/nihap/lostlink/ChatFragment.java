package com.nihap.lostlink;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class ChatFragment extends Fragment {
    private RecyclerView recyclerViewChatRooms;
    private SwipeRefreshLayout swipeRefreshChat;
    private LinearLayout layoutEmptyChats;
    private ChatRoomAdapter chatRoomAdapter;
    private List<ChatRoom> chatRooms;
    private ChatManager chatManager;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatRooms = new ArrayList<>();
        chatManager = new ChatManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadChatRooms();

        return view;
    }

    private void initializeViews(View view) {
        recyclerViewChatRooms = view.findViewById(R.id.recyclerViewChatRooms);
        swipeRefreshChat = view.findViewById(R.id.swipeRefreshChat);
        layoutEmptyChats = view.findViewById(R.id.layoutEmptyChats);
    }

    private void setupRecyclerView() {
        chatRoomAdapter = new ChatRoomAdapter(getContext(), chatRooms);
        recyclerViewChatRooms.setAdapter(chatRoomAdapter);
        recyclerViewChatRooms.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupSwipeRefresh() {
        swipeRefreshChat.setOnRefreshListener(this::loadChatRooms);
    }

    private void loadChatRooms() {
        swipeRefreshChat.setRefreshing(true);

        Log.d("ChatFragment", "Loading chat rooms...");

        chatManager.getUserChatRooms(new ChatManager.ChatRoomsCallback() {
            @Override
            public void onSuccess(List<ChatRoom> newChatRooms) {
                if (getContext() == null) return;

                Log.d("ChatFragment", "Loaded " + newChatRooms.size() + " chat rooms");

                chatRooms.clear();
                chatRooms.addAll(newChatRooms);
                chatRoomAdapter.updateChatRooms(chatRooms);

                updateEmptyState();
                swipeRefreshChat.setRefreshing(false);
            }

            @Override
            public void onError(Exception e) {
                if (getContext() == null) return;

                Log.e("ChatFragment", "Error loading chats: " + e.getMessage(), e);
                Toast.makeText(getContext(), "Error loading chats: " + e.getMessage(),
                             Toast.LENGTH_SHORT).show();
                swipeRefreshChat.setRefreshing(false);
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (chatRooms.isEmpty()) {
            recyclerViewChatRooms.setVisibility(View.GONE);
            layoutEmptyChats.setVisibility(View.VISIBLE);
        } else {
            recyclerViewChatRooms.setVisibility(View.VISIBLE);
            layoutEmptyChats.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChatRooms();
    }
}
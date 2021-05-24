package com.example.firebaseoc.mentor_chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.RequestManager;
import com.example.firebaseoc.databinding.ActivityMentorChatItemBinding;
import com.example.firebaseoc.models.Message;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Yassine Abou on 5/19/2021.
 */
public class MentorChatAdapter extends FirestoreRecyclerAdapter<Message, MessageViewHolder> {

    public interface Listener {
        void onDataChanged();
    }
    //FOR DATA
    private final RequestManager glide;
    private final String idCurrentUser;
    //FOR COMMUNICATION
    private Listener callback;


    public MentorChatAdapter(@NonNull FirestoreRecyclerOptions<Message> options, RequestManager glide, Listener callback, String idCurrentUser) {
        super(options);
        this.glide = glide;
        this.callback = callback;
        this.idCurrentUser = idCurrentUser;

    }

    @Override
    protected void onBindViewHolder(@NonNull @NotNull MessageViewHolder holder, int position, @NonNull @NotNull Message model) {
        holder.updateWithMessage(model, idCurrentUser, glide);
    }

    @NonNull
    @NotNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new MessageViewHolder(ActivityMentorChatItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        callback.onDataChanged();
    }
}

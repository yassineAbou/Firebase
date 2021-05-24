package com.example.firebaseoc.mentor_chat;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.firebaseoc.App;
import com.example.firebaseoc.R;
import com.example.firebaseoc.databinding.ActivityMentorChatItemBinding;
import com.example.firebaseoc.models.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Yassine Abou on 5/19/2021.
 */
public class MessageViewHolder extends RecyclerView.ViewHolder {

    private ActivityMentorChatItemBinding binding;

    //FOR DATA
    private final int colorCurrentUser;
    private final int colorRemoteUser;


    public MessageViewHolder(ActivityMentorChatItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        colorCurrentUser =  ContextCompat.getColor(App.getContext(), R.color.colorAccent);
        colorRemoteUser = ContextCompat.getColor(App.getContext(), R.color.colorPrimary);
    }

    @SuppressLint("NewApi")
    public void updateWithMessage(Message message, String currentUserId, RequestManager glide) {

        // Check if current user is the sender
        Boolean isCurrentUser = message.getUserSender().getUid().equals(currentUserId);

        // Update message TextView
        binding.textMessage.setText(message.getMessage());
        binding.textMessage.setTextAlignment(isCurrentUser ? View.TEXT_ALIGNMENT_TEXT_END : View.TEXT_ALIGNMENT_TEXT_START);


        // Update date TextView
        if (message.getDateCreated() != null) {
            binding.dateMessage.setText(convertDateToHour(message.getDateCreated()));
        }

        // Update isMentor ImageView
        binding.isMentorImage.setVisibility(message.getUserSender().getIsMentor() ? View.VISIBLE : View.INVISIBLE);

        // Update profile picture ImageView
        if (message.getUserSender().getUrlPicture() != null) {
            glide.load(message.getUserSender().getUrlPicture())
             .into(binding.profileImage);
        }

        // Update image sent ImageView
        if (message.getUrlImage() != null) {
            glide.load(message.getUrlImage())
                    .into(binding.imageSent);
            binding.imageSent.setVisibility(View.VISIBLE);
        } else {
            binding.imageSent.setVisibility(View.INVISIBLE);
        }

        //Update Message Bubble Color Background
        ((GradientDrawable) binding.textMessageContainer.getBackground()).setColor(isCurrentUser ?
                colorCurrentUser : colorRemoteUser);

        //Update all views alignment depending is current user or not
        updateDesignDependingUser(isCurrentUser);
    }

    private void updateDesignDependingUser(Boolean isSender){

        // PROFILE CONTAINER
        RelativeLayout.LayoutParams paramsLayoutHeader = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsLayoutHeader.addRule(isSender ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);
        binding.profileContainer.setLayoutParams(paramsLayoutHeader);

        // MESSAGE CONTAINER
        RelativeLayout.LayoutParams paramsLayoutContent = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsLayoutContent.addRule(isSender ? RelativeLayout.LEFT_OF : RelativeLayout.RIGHT_OF, R.id.profile_container);
        binding.messageContainer.setLayoutParams(paramsLayoutContent);

        // CARDVIEW IMAGE SEND
        RelativeLayout.LayoutParams paramsImageView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsImageView.addRule(isSender ? RelativeLayout.ALIGN_LEFT : RelativeLayout.ALIGN_RIGHT, R.id.text_message_container);
        binding.imageSentCardView.setLayoutParams(paramsImageView);

        binding.activityMentorChatItemRootView.requestLayout();
    }

    // ---
    private String convertDateToHour(Date date) {
        @SuppressLint("SimpleDateFormat") DateFormat dfTime = new SimpleDateFormat("HH:mm");
        return dfTime.format(date);
    }



}

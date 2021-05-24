package com.example.firebaseoc.api;

import com.example.firebaseoc.models.Message;
import com.example.firebaseoc.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

/**
 * Created by Yassine Abou on 5/19/2021.
 */
public class MessageHelper {

    public static final String COLLECTION_NAME = "messages";

    // --- GET ---
    public static Query getAllMessageForChat(String chat) {
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .orderBy("dateCreated")
                .limit(50);
    }

    public static Task<DocumentReference> createMessageForChat(String textMessage, String chat,
       User userSender) {

        //Create the Message object
        Message message = new Message(textMessage, userSender);

        //Store Message to Firestore
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .add(message);
    }

    public static Task<DocumentReference> createMessageWithImgForChat(String urlImg,
      String txtMessage, String chat, User userSender) {

        //Creating Message with the URL image
        Message message = new Message(txtMessage, urlImg, userSender);

        //Storing Message on Firestore
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .add(message);
    }


}

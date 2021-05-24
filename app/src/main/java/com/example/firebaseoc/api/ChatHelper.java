package com.example.firebaseoc.api;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Created by Yassine Abou on 5/19/2021.
 */
public class ChatHelper {

    public static final String COLLECTION_NAME = "chats";

    // --- COLLECTION REFERENCE ---
    public static CollectionReference getChatCollection() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }
}

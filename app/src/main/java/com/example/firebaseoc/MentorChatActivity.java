package com.example.firebaseoc;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.firebaseoc.api.MessageHelper;
import com.example.firebaseoc.api.UserHelper;
import com.example.firebaseoc.databinding.ActivityMentorChatBinding;
import com.example.firebaseoc.mentor_chat.MentorChatAdapter;
import com.example.firebaseoc.models.Message;
import com.example.firebaseoc.models.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.example.firebaseoc.auth.ProfileActivity.getCurrentUser;

public class MentorChatActivity extends AppCompatActivity implements MentorChatAdapter.Listener {

    private ActivityMentorChatBinding binding;

    //--  FOR DATA --

    //Declaring Adapter and data
    private MentorChatAdapter mMentorChatAdapter;
    @Nullable
    private User modelCurrentUser;
    private String currentChatName;
    // STATIC DATA FOR CHAT
    public static final String CHAT_NAME_ANDROID = "android";
    public static final String CHAT_NAME_FIREBASE = "firebase";
    public static final String CHAT_NAME_BUG = "bug";
    // STATIC DATA FOR PICTURE
    private static final String PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int RC_IMG_PERMS = 100;
    //Uri of image selected by user
    private Uri uriImgSelected;
    //STATIC DATA FOR PICTURE
    private static final int RC_CHOOSE_PHOTO = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMentorChatBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        configureRecyclerView(CHAT_NAME_ANDROID);
        getCurrentUserFromFirestore();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Calling the appropriate method after activity result
        handleResponse(requestCode, resultCode, data);
    }

    //----------------------
    // ACTIONS
    //---------------------


    public void onClickSendMessage(View view) {
        if (!TextUtils.isEmpty(binding.messageEditText.getText()) && modelCurrentUser != null) {

            if (binding.imgChosenPreview.getDrawable() == null) {
                MessageHelper.createMessageForChat(Objects.requireNonNull(binding.messageEditText.getText()).toString(), this.currentChatName, modelCurrentUser).addOnFailureListener(this.onFailureListener());
                binding.messageEditText.setText("");
            } else {
                // SEND A IMAGE + TEXT IMAGE
                uploadPhotoInFirebaseAndSendMessage(binding.messageEditText.getText().toString());
                binding.messageEditText.setText("");
                binding.imgChosenPreview.setImageDrawable(null);
            }

        }
    }

        public void onClickChatButtons (View view){
            // Re-Configure the RecyclerView depending chosen chat
            switch (Integer.valueOf(view.getTag().toString())) {
                case 10:
                    configureRecyclerView(CHAT_NAME_ANDROID);
                    break;
                case 20:
                    configureRecyclerView(CHAT_NAME_FIREBASE);
                    break;
                case 30:
                    configureRecyclerView(CHAT_NAME_BUG);
                    break;
            }
        }

        //Calling the appropriate method
        @AfterPermissionGranted(RC_IMG_PERMS)
        public void onClickAddFile (View view){
            chooseImgFromPhone();
        }


        // --------------------
        // REST REQUESTS
        // --------------------

        //Get Current User from Firestore
        private void getCurrentUserFromFirestore () {
            UserHelper.getUser(Objects.requireNonNull(getCurrentUser()).getUid()).addOnSuccessListener(documentSnapshot -> modelCurrentUser = documentSnapshot.toObject(User.class));
        }

         //Upload a picture in Firebase and send a message
         private void uploadPhotoInFirebaseAndSendMessage(final String message) {
             String uuid = UUID.randomUUID().toString(); // GENERATE UNIQUE STRING
             // A - UPLOAD TO GCS
             StorageReference mImgRef = FirebaseStorage.getInstance().getReference(uuid);
             UploadTask uploadTask = mImgRef.putFile(uriImgSelected);
             uploadTask.addOnSuccessListener(taskSnapshot -> {
                 Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                 firebaseUri.addOnSuccessListener(uri -> {

                     String url = uri.toString();

                     // B - SAVE MESSAGE IN FIRESTORE
                     MessageHelper.createMessageWithImgForChat(url, message, currentChatName, modelCurrentUser)
                             .addOnFailureListener(onFailureListener());
                 });
             })
                     .addOnFailureListener(onFailureListener());
         }

    // --------------------
        // UI
        // --------------------

        //Configure RecyclerView with a Query
        private void configureRecyclerView (String chatName){
            currentChatName = chatName;
            //Configure Adapter & RecyclerView
            mMentorChatAdapter = new
                    MentorChatAdapter(generateOptionsForAdapter(MessageHelper.
                    getAllMessageForChat(currentChatName)), Glide.with(this), this, Objects.requireNonNull(getCurrentUser()).getUid());
            mMentorChatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    // Scroll to bottom on new messages
                    binding.recyclerView.smoothScrollToPosition(mMentorChatAdapter.getItemCount());
                }
            });
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerView.setAdapter(mMentorChatAdapter);
        }

        //Create options for RecyclerView from a Query
        private FirestoreRecyclerOptions<Message> generateOptionsForAdapter (Query query){
            return new FirestoreRecyclerOptions.Builder<Message>()
                    .setQuery(query, Message.class)
                    .setLifecycleOwner(this)
                    .build();
        }

        // --------------------
        // FILE MANAGEMENT
        // --------------------

        private void chooseImgFromPhone () {
            if (!EasyPermissions.hasPermissions(this, PERMS)) {
                EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_files_access),
                        RC_IMG_PERMS, PERMS);
                return;
            }
            //Launch an "Selection Image" Activity
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RC_CHOOSE_PHOTO);
        }

        //Handle activity response (after user has chosen or not a picture)
        private void handleResponse ( int requestCode, int resultCode, Intent data){
            if (requestCode == RC_CHOOSE_PHOTO) { //SUCCESS
                uriImgSelected = data.getData();
                Glide.with(this) //SHOWING PREVIEW OF IMAGE
                        .load(uriImgSelected)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.imgChosenPreview);
            }
        }

        // --------------------
        // CALLBACK
        // --------------------

        @Override
        public void onDataChanged () {
            //Show TextView in case RecyclerView is empty
            binding.textViewEmpty.setVisibility(mMentorChatAdapter.getItemCount() == 0 ? View.VISIBLE :
                    View.GONE);
        }

        // --------------------
        // ERROR HANDLER
        // --------------------

        protected OnFailureListener onFailureListener () {
            return e -> Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
        }

    }

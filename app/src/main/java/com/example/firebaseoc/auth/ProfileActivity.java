package com.example.firebaseoc.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.firebaseoc.R;
import com.example.firebaseoc.api.UserHelper;
import com.example.firebaseoc.databinding.ActivityProfileBinding;
import com.example.firebaseoc.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    //FOR DATA
    //Identify each Http Request
    public static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    // Creating identifier to identify REST REQUEST (Update username)
    public static final int UPDATE_USERNAME = 30;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        updateUIWhenCreating(); //Update UI


    }

    // --------------------
    // ACTIONS
    // --------------------

    //Adding requests to button listeners
    public void onClickUpdateButton(View view) { updateUsernameInFirebase();  }

    public void onClickCheckBoxIsMentor(View view) { updateUserIsMentor();  }

    public void onClickSignOutButton(View view) { signOutFromFirebase(); }

    public void onClickDeleteButton(View view) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_account)
                .setPositiveButton(R.string.popup_message_choice_yes, (dialog, which) -> deleteUserFromFirebase())
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show();
    }

    // --------------------
    // REST REQUESTS
    // --------------------

    //Create http requests (SignOut & Delete)
    private void signOutFromFirebase() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private void deleteUserFromFirebase() {
        //We also delete user from firestore storage
        UserHelper.deleteUser(Objects.requireNonNull(getCurrentUser()).getUid()).addOnFailureListener(onFailureListener());

        AuthUI.getInstance()
                .delete(this)
                .addOnSuccessListener(this, updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
    }



    //Update User Username
    private void updateUsernameInFirebase() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String username = Objects.requireNonNull(binding.editTextUsername.getText()).toString();

        if (getCurrentUser() != null) {
            if (!username.isEmpty() && !username.equals(getString(R.string.info_no_username_found))) {

                UserHelper.updateUsername(username, getCurrentUser().getUid())
                        .addOnFailureListener(onFailureListener())
                        .addOnSuccessListener(updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME));
            }
        }
    }

    //Update User Mentor (is or not)
    private void updateUserIsMentor() {
        if (getCurrentUser() != null) {
            UserHelper.updateIsMentor(getCurrentUser().getUid(), binding.checkBoxIsMentor.isChecked())
                    .addOnFailureListener(onFailureListener());
        }
    }

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    public static FirebaseUser getCurrentUser() { return FirebaseAuth.getInstance().getCurrentUser(); }

    public static Boolean isCurrentUserLogged() { return (getCurrentUser() != null); }

    // --------------------
    // UI
    // --------------------

    //Update UI when activity is creating
    private void updateUIWhenCreating() {

        if (getCurrentUser() != null) {

            //Get picture URL from Firebase
            if (getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.imageviewProfile);
            }

            //Get email & username from Firebase
            String email = TextUtils.isEmpty(getCurrentUser().getEmail()) ?
          getString(R.string.info_no_email_found) : getCurrentUser().getEmail();
            binding.textViewEmail.setText(email);
            //Get additional data from Firestore (isMentor & Username)
            UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> {
                User currentUser = documentSnapshot.toObject(User.class);
                assert currentUser != null;
                String username = TextUtils.isEmpty(currentUser.getUrlPicture()) ?
                        getString(R.string.info_no_username_found) : currentUser.getUsername();
                binding.checkBoxIsMentor.setChecked(currentUser.getIsMentor());
                binding.editTextUsername.setText(username);
            });
        }
    }

    //Create OnCompleteListener called after tasks ended
    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int original) {
        return aVoid -> {
            switch (original) {
                //Hiding Progress bar after request completed
                case UPDATE_USERNAME:
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    break;
                case SIGN_OUT_TASK:
                case DELETE_USER_TASK:
                    finish();
                    break;
                default:
                    break;
            }
        };
    }

    // --------------------
    // ERROR HANDLER
    // --------------------

    protected OnFailureListener onFailureListener() {
        return e -> Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
    }

}
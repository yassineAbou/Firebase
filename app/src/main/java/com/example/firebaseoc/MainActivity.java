package com.example.firebaseoc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.firebaseoc.api.UserHelper;
import com.example.firebaseoc.auth.ProfileActivity;
import com.example.firebaseoc.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

import static com.example.firebaseoc.auth.ProfileActivity.getCurrentUser;
import static com.example.firebaseoc.auth.ProfileActivity.isCurrentUserLogged;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


    }

    @Override
    protected void onResume() {
        super.onResume();
        //Update UI when activity is resuming
        updateUIWhenResuming();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Handle SignIn Activity response on activity result
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    // --------------------
    // ACTIONS
    // --------------------

    public void onClickChatButton(View view) {
        //Check if user is connected before launching MentorActivity
        if (isCurrentUserLogged()) {
            startMentorChatActivity();
        } else {
            this.showSnackBar(binding.coordinatorLayout, getString(R.string.error_not_connected));
        }

    }

    public void onClickLoginButton(View view) {
        //Start appropriate activity
        if (isCurrentUserLogged()) {
            startProfileActivity();
        } else {
            startSignInActivity();
        }

    }

    // --------------------
    // NAVIGATION
    // --------------------

    //Launch Sign-In Activity
    private void startSignInActivity() {


        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.FacebookBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.mipmap.ic_launcher1)
                        .build(),
                RC_SIGN_IN);
    }

    //Launching Profile Activity
    private void startProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    // 1 - Starting Mentor Activity
    private void startMentorChatActivity() {
        Intent intent = new Intent(this, MentorChatActivity.class);
        startActivity(intent);
    }

    // --------------------
    // UI
    // --------------------

    //Show Snack Bar with a message
    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    //Update UI when activity is resuming
    private void updateUIWhenResuming() {
        binding.buttonLogin.setText(isCurrentUserLogged() ?
                getString(R.string.button_login_text_logged) : getString(R.string.button_login_text_not_logged));
    }

    // --------------------
    // UTILS
    // --------------------

    //Method that handles response after SignIn Activity close
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                createUserInFirestore();
                // 2 - CREATE USER IN FIRESTORE
                showSnackBar(binding.coordinatorLayout, getString(R.string.connection_succeed));
            } else { // ERRORS
                if (response == null) {
                    showSnackBar(binding.coordinatorLayout, getString(R.string.error_authentication_canceled));
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(binding.coordinatorLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(binding.coordinatorLayout, getString(R.string.error_unknown_error));
                }
            }
        }
    }

    // 1 - Http request that create user in firestore

    private void createUserInFirestore() {

        if (getCurrentUser() != null) {

            String urlPicture = (getCurrentUser().getPhotoUrl() != null) ? getCurrentUser().getPhotoUrl().toString() : null;
            String username = getCurrentUser().getDisplayName();
            String uid = getCurrentUser().getUid();

            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());

        }

    }


    // --------------------
    // ERROR HANDLER
    // --------------------

    protected OnFailureListener onFailureListener() {
        return e -> Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
    }

}
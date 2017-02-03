package com.leeway.testfirebase;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    TextView tvProfile;
    EditText editTextEmail;
    EditText editTextPassword;
    Button btnSignin;
    Button btnCreateAccount;
    Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initInstances();
    }

    private void initInstances() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                }
                updateUI(user);
            }
        };

        tvProfile = (TextView) findViewById(R.id.tvProfile);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        btnSignin = (Button) findViewById(R.id.btnSignin);
        btnCreateAccount = (Button) findViewById(R.id.btnCreateAccount);
        btnSignOut = (Button) findViewById(R.id.btnSignOut);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            if (user.getPhotoUrl() != null) {
                // download image
            }
            tvProfile.setText("DisplayName: " + user.getDisplayName());
            tvProfile.append("\n\n");
            tvProfile.append("Email: " + user.getEmail());
            tvProfile.append("\n\n");
            tvProfile.append("Firebase ID: " + user.getUid());

            editTextEmail.setVisibility(View.GONE);
            editTextPassword.setVisibility(View.GONE);
            btnSignin.setVisibility(View.GONE);
            btnCreateAccount.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
        } else {
            tvProfile.setText(null);

            editTextEmail.setVisibility(View.VISIBLE);
            editTextPassword.setVisibility(View.VISIBLE);
            btnSignin.setVisibility(View.VISIBLE);
            btnCreateAccount.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
        }
        hideProgressDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}

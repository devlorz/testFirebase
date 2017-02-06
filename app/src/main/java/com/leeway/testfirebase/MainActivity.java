package com.leeway.testfirebase;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager mCallbackManager;

    TextView tvProfile;
    EditText editTextEmail;
    EditText editTextPassword;
    Button btnSignin;
    Button btnCreateAccount;
    Button btnSignOut;
    LoginButton btnFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
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
        mCallbackManager = CallbackManager.Factory.create();

        tvProfile = (TextView) findViewById(R.id.tvProfile);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        btnSignin = (Button) findViewById(R.id.btnSignin);
        btnCreateAccount = (Button) findViewById(R.id.btnCreateAccount);
        btnSignOut = (Button) findViewById(R.id.btnSignOut);
        btnFacebook = (LoginButton) findViewById(R.id.btnFacebookLogin);

        btnSignin.setOnClickListener(this);
        btnCreateAccount.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnFacebook.setReadPermissions("email", "public_profile");
        btnFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAcessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                updateUI(null);
            }

            @Override
            public void onError(FacebookException error) {
                updateUI(null);
            }
        });
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCreateAccount:
                createAccount(editTextEmail.getText().toString(), editTextPassword.getText().toString());
                break;
            case R.id.btnSignOut:
                signOut();
                break;
        }
    }

    private void signOut() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.logout);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                updateUI(null);
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }

    private boolean validateForm() {
        if (editTextEmail.getText().toString().equals("")) {
            editTextEmail.setError("Required.");
            return false;
        } else if (editTextPassword.getText().toString().equals("")) {
            editTextPassword.setError("Required.");
            return false;
        } else {
            editTextEmail.setError(null);
            return true;
        }
    }

    private void createAccount(String email, String password) {
        if(!validateForm()) return;
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()) {
                    tvProfile.setTextColor(Color.RED);
                    tvProfile.setText(task.getException().getMessage());
                } else {
                    tvProfile.setTextColor(Color.DKGRAY);
                }
                hideProgressDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAcessToken(AccessToken token) {
        showProgressDialog();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()) {
                    tvProfile.setTextColor(Color.RED);
                    tvProfile.setText(task.getException().getMessage());
                } else {
                    tvProfile.setTextColor(Color.DKGRAY);
                }
                hideProgressDialog();
            }
        });
    }
}

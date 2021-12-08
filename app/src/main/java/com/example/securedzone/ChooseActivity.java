package com.example.securedzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChooseActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private EditText phone_tv, pass_tv;
    private Button login_btn;
    private ProgressDialog loader;
    private String username;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private SignInButton signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        mAuth = FirebaseAuth.getInstance();
        // Set the dimensions of the sign-in button.
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        findViewById(R.id.create_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChooseActivity.this, RegisterActivity.class));
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        phone_tv = findViewById(R.id.phone_tv);
        pass_tv = findViewById(R.id.pass_tv);
        login_btn = findViewById(R.id.login_btn);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loader = new ProgressDialog(ChooseActivity.this);
                loader.setTitle("Checking credentials");
                loader.setMessage("Verifying your credentials with database");
                loader.show();
                Boolean isCredentialsCorrect = CheckCredentials(phone_tv.getText().toString().replace(" ", ""), pass_tv.getText().toString().replace(" ", ""));
                if (isCredentialsCorrect) {
                    Intent go = new Intent(ChooseActivity.this, DashBoardActivity.class);
                    go.putExtra("name", username);
                    startActivity(go);
                }

            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog loader = new ProgressDialog(ChooseActivity.this);
                loader.setTitle("Checking credentials");
                loader.show();
                signIn();
            }
        });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("auth", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("auth", "Google sign in failed", e);
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("auth", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(ChooseActivity.this, DashBoardActivity.class).putExtra("name", user.getDisplayName()));

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("auth", "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private Boolean CheckCredentials(String phone, String pass) {
        final boolean[] result = new boolean[1];
        String Epass = RegisterActivity.Md5Hash(pass);
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(phone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("encryption").getValue().toString().equals("enabled")) {
                        if (snapshot.child("pass").getValue().toString().equals(Epass)) {
                            result[0] = true;
                            username = snapshot.child("name").getValue().toString();
                        } else {
                            Toast.makeText(getApplicationContext(), "wrong crendentials", Toast.LENGTH_SHORT).show();

                        }

                    } else {
                        if (snapshot.child("pass").getValue().toString().equals(pass)) {
                            result[0] = true;
                            username = snapshot.child("name").getValue().toString();
                        } else {
                            Toast.makeText(getApplicationContext(), "wrong crendentials", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    result[0] = false;
                    Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();
                }
                loader.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Server unavailable", Toast.LENGTH_SHORT).show();
                result[0] = false;
                loader.dismiss();
            }
        });

        return result[0];
    }
}

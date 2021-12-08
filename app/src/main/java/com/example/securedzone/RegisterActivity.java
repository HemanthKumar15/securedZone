package com.example.securedzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {
    private TextView title;
    private EditText name_tv,phone_tv,otp_tv,pass_tv,pass_tv2;
    private CheckBox isEncrypted;
    private Button next_btn;
    private CardView otp_card;
    private ImageView cancel_view;
    private FirebaseAuth mAuth;
    private String id;
    private FloatingActionButton verify;
    private ProgressDialog loader;
    private String fpass,fname,fnum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        title =findViewById(R.id.title);
        name_tv =findViewById(R.id.name_tv);
        phone_tv =findViewById(R.id.phone_tv);
        otp_tv =findViewById(R.id.otp_tv);
        pass_tv =findViewById(R.id.pass_tv);
        pass_tv2 =findViewById(R.id.pass_tv2);
        isEncrypted =findViewById(R.id.isEncrypted);
        next_btn =findViewById(R.id.next_btn);
        otp_card = findViewById(R.id.otp_card);
        cancel_view = findViewById(R.id.cancel_view);
        verify = findViewById(R.id.verify);
        mAuth = FirebaseAuth.getInstance();
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phone_tv.getText().toString().replace(" ","").equals("")){
                    Toast.makeText(getApplicationContext(), "Please enter the phone number", Toast.LENGTH_SHORT).show();
                }
                else if(!(phone_tv.getText().toString().replace(" ","").length() == 10)){
                    Toast.makeText(getApplicationContext(), "Please enter a 10-digit phone number", Toast.LENGTH_SHORT).show();
                }
                else if(!(pass_tv.getText().toString().equals(pass_tv2.getText().toString()))){
                    Toast.makeText(getApplicationContext(), "Password and confirm password are not same", Toast.LENGTH_SHORT).show();
                }
                else{
                    OtpDialog(name_tv.getText().toString(),"+91"+phone_tv.getText().toString().replace(" ",""),pass_tv.getText().toString());
                }
            }
        });
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loader = new ProgressDialog(RegisterActivity.this);
                loader.setTitle("Checking Credentials");
                loader.setMessage("Verifying the OTP and checking your credentials");
                loader.show();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(id, otp_tv.getText().toString().replace(" ",""));
                signInWithPhoneAuthCredential(credential);
            }
        });
        cancel_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisableOtpDialog();
            }
        });


    }

    private void DisableOtpDialog() {
        title.setVisibility(View.VISIBLE);
        name_tv.setVisibility(View.VISIBLE);
        pass_tv.setVisibility(View.VISIBLE);
        pass_tv2.setVisibility(View.VISIBLE);
        phone_tv.setVisibility(View.VISIBLE);
        next_btn.setVisibility(View.VISIBLE);
        isEncrypted.setVisibility(View.VISIBLE);
        otp_card.setVisibility(View.GONE);
    }

    private void OtpDialog(String name, String number, String password) {
        fname = name;
        fpass = password;
        fnum = number;
        sendVerificationCode(number);
        title.setVisibility(View.GONE);
        name_tv.setVisibility(View.GONE);
        pass_tv.setVisibility(View.GONE);
        pass_tv2.setVisibility(View.GONE);
        phone_tv.setVisibility(View.GONE);
        next_btn.setVisibility(View.GONE);
        isEncrypted.setVisibility(View.GONE);
        otp_card.setVisibility(View.VISIBLE);
    }


    private void sendVerificationCode(String number) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeSent(@NonNull String id, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        RegisterActivity.this.id = id;
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(RegisterActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });        // OnVerificationStateChangedCallbacks


    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> Utask) {
                        if (Utask.isSuccessful()) {
                            if(isEncrypted.isChecked()){
                                String enkey = Md5Hash(fpass);
                                HashMap<String,Object> map = new HashMap<>();
                                map.put("name",fname);
                                map.put("pass",enkey);
                                map.put("num",fnum);
                                map.put("encryption","enabled");
                                FirebaseDatabase.getInstance().getReference().child("users")
                                        .child(fnum).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            startActivity(new Intent(getApplicationContext(),DashBoardActivity.class).putExtra("name",fname));
                                            finish();
                                            FirebaseUser user = Utask.getResult().getUser();
                                        }
                                    }
                                });
                            }
                            else{
                                HashMap<String,Object> map = new HashMap<>();
                                map.put("name",fname);
                                map.put("pass",fpass);
                                map.put("num",fnum);
                                map.put("encryption","disabled");
                                FirebaseDatabase.getInstance().getReference().child("users")
                                        .child(fnum).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            startActivity(new Intent(getApplicationContext(),DashBoardActivity.class).putExtra("name",fname));
                                            finish();
                                            FirebaseUser user = Utask.getResult().getUser();
                                        }
                                    }
                                });
                            }

                            loader.dismiss();
                            // ...
                        } else {
                            Toast.makeText(getApplicationContext(), "Verification Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    public static String Md5Hash(String s){
        StringBuffer MD5Hash = new StringBuffer();
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();


            for (int i = 0;i<messageDigest.length;i++){
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() <2){
                    h= "0"+h;
                    MD5Hash.append(h);
                }
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return String.valueOf(MD5Hash);
    }


}
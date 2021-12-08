package com.example.securedzone;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scanRoot();
            }
        },3000);
    }

    private void scanRoot() {
        if(RootUtil.isDeviceRooted()){
            new AlertDialog.Builder(this)
                    .setTitle("Rooted devices detected!!")
                    .setMessage("This app does not support rooted devices due to security purpose")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .show();
        }
        else {
            startActivity(new Intent(MainActivity.this,ChooseActivity.class));
            finish();

        }

    }
}
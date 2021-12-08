package com.example.securedzone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class DashBoardActivity extends AppCompatActivity {

    private TextView welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        welcome = findViewById(R.id.wlc);
        welcome.setText("Welcome, "+getIntent().getStringExtra("name"));
    }
}
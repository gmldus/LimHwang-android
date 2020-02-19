package com.example.a2020limhwang;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MypageActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    Button back;
    TextView student_major, student_number, student_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        back = findViewById(R.id.back);
        student_major = findViewById(R.id.student_major);
        student_number = findViewById(R.id.student_number);
        student_name = findViewById(R.id.student_name);

        sharedPreferences = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String major = sharedPreferences.getString("major", "");
        String number = sharedPreferences.getString("id_students", "");
        String name = sharedPreferences.getString("name_students", "");

        student_major.setText(major);
        student_number.setText(number);
        student_name.setText(name);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();
        }
        else if (v.getId() == R.id.logoutButton) {
            Intent intent = new Intent (this, MainActivity.class);
            editor.clear();
            editor.commit();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }
    }
}

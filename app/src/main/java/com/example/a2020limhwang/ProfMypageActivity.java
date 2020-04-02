package com.example.a2020limhwang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfMypageActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    ImageButton back;
    TextView prof_major, prof_number, prof_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profmypage);

        back = findViewById(R.id.back);
        prof_major = findViewById(R.id.prof_major);
        prof_number = findViewById(R.id.prof_number);
        prof_name = findViewById(R.id.prof_name);

        sharedPreferences = getSharedPreferences("pFile", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String major = sharedPreferences.getString("major", "");
        String number = sharedPreferences.getString("id_professors", "");
        String name = sharedPreferences.getString("name_professors", "");

        prof_major.setText(major);
        prof_number.setText(number);
        prof_name.setText(name);
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

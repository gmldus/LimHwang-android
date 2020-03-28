package com.example.a2020limhwang;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfLectureActivity extends AppCompatActivity {
    ImageButton back, profile;
    ListView listView;
    private SharedPreferences sharedPreferences;
    String[] lectureNum, lectureName, startTime, endTime;
    String id_prof;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proflecture);

        Intent intent = getIntent();
        lectureNum = intent.getStringArrayExtra("lectureNum");
        sharedPreferences = getSharedPreferences("pFile", MODE_PRIVATE);
        id_prof = sharedPreferences.getString("id_prof", "");

        back = findViewById(R.id.back);
        profile = findViewById(R.id.profile);
        listView= findViewById(R.id.listView);
    }

    public class CustomList extends ArrayAdapter<String> {
        private final Activity context;
        public CustomList(Activity context ) {
            super(context, R.layout.item, lectureName);
            this.context = context;
        }
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView= inflater.inflate(R.layout.item, null, true);
            TextView tv_name = rowView.findViewById(R.id.name);
            TextView tv_time1 = rowView.findViewById(R.id.time1);
            TextView tv_time2 = rowView.findViewById(R.id.time2);
            TextView tv_percentage = rowView.findViewById(R.id.percentage);

            tv_name.setText(lectureName[position]); // + 분반번호
            tv_time1.setText(startTime[position]);
            tv_time2.setText(endTime[position]);

            return rowView;
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.profile) {
            Intent intent = new Intent(this, ProfMypageActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.back) {
            finish();
        }
    }

}

package com.example.a2020limhwang;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ListActivity extends AppCompatActivity {

    Button back, profile;
    ListView listView;

    String[] name = {
            "관현악과",
            "글로벌협력전공",
            "기계시스템학부",
            "독일언어·문화학과",
            "미디어학부",
            "소프트웨어융합전공",
            "시각·영상디자인과",
            "작곡과",
            "피아노과",
            "행정학과"
    } ;

    String[] time1 = {
            "Orchestral Instruments",
            "Global Cooperation",
            "Mechanical Systems Engineering",
            "German Language & Culture",
            "Communication & Media",
            "Software Convergence",
            "Visual & Media Design",
            "Composition",
            "Piano",
            "Public Administration"
    } ;

    String[] time2 = {
            "1906",
            "1906",
            "1906",
            "1906",
            "1906",
            "1906",
            "1906",
            "1906",
            "1906",
            "1906"
    } ;

    Integer[] percentage = {
            90,
            23,
            34,
            23,
            45,
            56,
            86,
            78,
            45,
            56
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        back = findViewById(R.id.back);
        profile = findViewById(R.id.profile);
        listView= findViewById(R.id.listView);

        CustomList adapter = new CustomList(ListActivity.this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(getBaseContext(), name[+position], Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class CustomList extends ArrayAdapter<String> {
        private final Activity context;
        public CustomList(Activity context ) {
            super(context, R.layout.item, name);
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

            tv_name.setText(name[position]);
            tv_time1.setText(time1[position]);
            tv_time2.setText(time2[position]);
            tv_percentage.setText(percentage[position] + "%");

            if (percentage[position] <= 75) {
                tv_percentage.setTextColor(Color.parseColor("#CB333B"));
            }
            else if (percentage[position] > 75 && percentage[position] < 80) {
                tv_percentage.setTextColor(Color.parseColor("#E1CD00"));
            }
            else if (percentage[position] >= 80 && percentage[position] < 90) {
                tv_percentage.setTextColor(Color.parseColor("#34B78F"));
            }
            else {
                tv_percentage.setTextColor(Color.parseColor("#3A5DAE"));
            }

            return rowView;
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.profile) {
            Intent intent = new Intent(this, MypageActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.back) {
            finish();
        }
    }
}

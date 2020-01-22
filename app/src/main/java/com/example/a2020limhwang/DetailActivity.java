package com.example.a2020limhwang;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    Button back, profile, btn_edit;
    ListView listView;

    String[] date = {
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
    };
    String[] checktime = {
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
    };
    String[] state = {
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        back = findViewById(R.id.back);
        profile = findViewById(R.id.profile);
        btn_edit = findViewById(R.id.btn_edit);

        listView=(ListView)findViewById(R.id.list_detail);

        CustomList adapter = new CustomList(DetailActivity.this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getBaseContext(), date[+position], Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class CustomList extends ArrayAdapter<String> {
        private final Activity context;
        public CustomList(Activity context ) {
            super(context, R.layout.item_detail, date);
            this.context = context;
        }
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView= inflater.inflate(R.layout.item_detail, null, true);
            TextView tv_date = (TextView) rowView.findViewById(R.id.date);
            TextView tv_checktime = (TextView) rowView.findViewById(R.id.checktime);
            TextView tv_state = (TextView) rowView.findViewById(R.id.state);

            tv_date.setText(date[position]);
            tv_checktime.setText(checktime[position]);
            tv_state.setText(state[position]);


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
        else if (v.getId() == R.id.btn_edit) {
            Intent intent = new Intent(this, EditActivity.class);
            startActivity(intent);
        }

    }

}

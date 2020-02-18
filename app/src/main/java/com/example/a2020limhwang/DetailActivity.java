package com.example.a2020limhwang;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    Button back, profile, btn_edit;
    ListView listView;
    TextView text_percentage, text_lectureName, untilF;
    int numOfAtt = 0, cntAtt = 0, cntLate = 0, cntAbs = 0;
    private SharedPreferences sharedPreferences;
    String lecture, id_students, str_result, lectureName;
    String[] date, name, lectureNum, state, checkTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        lecture = intent.getExtras().getString("lectureNum");
        lectureName = intent.getStringExtra("lectureName");

        back = findViewById(R.id.back);
        profile = findViewById(R.id.profile);
        btn_edit = findViewById(R.id.btn_edit);
        listView= findViewById(R.id.list_detail);
        text_percentage = findViewById(R.id.text_percentage);
        text_lectureName = findViewById(R.id.textView_lectureName);
        untilF = findViewById(R.id.score_untilF);

        text_lectureName.setText(lectureName);

        sharedPreferences = getSharedPreferences("sFile", MODE_PRIVATE);
        id_students = sharedPreferences.getString("id_students", "");

        new JSONTask().execute("http://172.30.1.23:3000/attendances/get");
        //untilF.setText(numOfAtt / 4 - (cntAbs + (cntLate / 3)));
        //untilF.setText(numOfAtt);
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
            tv_checktime.setText(checkTime[position]);
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

    public class JSONTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("id_students", id_students);
                jsonObject.accumulate("id_lectures", lecture);

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(urls[0]);

                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Cache-Control", "no-cache");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Accept", "text/html");
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.connect();

                    OutputStream outStream = con.getOutputStream();

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();

                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    return buffer.toString();
                }catch (MalformedURLException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            str_result = result+"";
            try{
                JSONObject jsonObject = new JSONObject(str_result);
                JSONArray attendanceInfoArray = jsonObject.getJSONArray("data");
                numOfAtt = attendanceInfoArray.length();

                name = new String[numOfAtt];
                lectureNum = new String[numOfAtt];
                date = new String[numOfAtt];
                state = new String[numOfAtt];
                checkTime = new String[numOfAtt];

                for (int i = 0; i < numOfAtt; i++) {
                    JSONObject tmp = (JSONObject)attendanceInfoArray.get(i);
                    name[i] = tmp.getString("id_students");
                    lectureNum[i] = tmp.getString("id_lectures");
                    date[i] = tmp.getString("date");
                    state[i] = tmp.getString("state");
                    checkTime[i] = tmp.getString("check_time");

                    if(Integer.parseInt(state[i]) == 0){
                        state[i] = "미정";
                    }
                    else if(Integer.parseInt(state[i]) == 1){
                        state[i] = "출석";
                        cntAtt++;
                    }
                    else if(Integer.parseInt(state[i]) == 2){
                        state[i] = "지각";
                        cntLate++;
                    }
                    else if(Integer.parseInt(state[i])== 3){
                        state[i] = "결석";
                        cntAbs++;
                    }

                    Log.d("name", name[i]);
                    Log.d("lectureNum", lectureNum[i]);
                    Log.d("date", date[i]);
                    //Log.d("state", state[i]);
                    Log.d("checktime", checkTime[i]);

                }

                Log.d("log all", numOfAtt+"");
                Log.d("log att", cntAtt+"");
                Log.d("log late", cntLate+"");
                Log.d("log abs", cntAbs+"");

                CustomList adapter = new CustomList(DetailActivity.this);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Toast.makeText(getBaseContext(), date[+position], Toast.LENGTH_SHORT).show();
                    }
                });

                int score = numOfAtt / 4 - (cntAbs + (cntLate / 3));
                Log.d("score", score+"");
                untilF.setText(score+"");

            }catch (JSONException e) {
                e.printStackTrace();
            }
        }



    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Intent intent = getIntent();
            ScoreInformation si = (ScoreInformation) intent.getSerializableExtra("score_information");
            text_percentage.setText(si.rate + "");
        }
    }

}

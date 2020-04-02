package com.example.a2020limhwang;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Arrays;

public class ProfLectureActivity extends AppCompatActivity {
    ImageButton back, profile;
    ListView listView;
    private SharedPreferences sharedPreferences;
    String[] lectureNum, lectureName, startTime, endTime;
    String id_prof;
    private String str_result;
    int numOfLec;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proflecture);

        Intent intent = getIntent();
        lectureNum = intent.getStringArrayExtra("lectureNum");
        sharedPreferences = getSharedPreferences("pFile", MODE_PRIVATE);
        id_prof = sharedPreferences.getString("id_professors", "");

        back = findViewById(R.id.back);
        profile = findViewById(R.id.profile);
        listView= findViewById(R.id.listView);

        //ip고치기
        new ProfLectureActivity.JSONTask().execute("http://192.168.0.76:3000/lectures/get");
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

    public class JSONTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("array", Arrays.toString(lectureNum));

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
            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            str_result = result+"";
            try{
                JSONObject jsonObject = new JSONObject(str_result);
                JSONArray lectureInfoArray = jsonObject.getJSONArray("lectureList");
                numOfLec = lectureInfoArray.length();

                lectureNum =new String[numOfLec];
                lectureName= new String[numOfLec];
                startTime = new String[numOfLec];
                endTime = new String[numOfLec];

                for (int i = 0; i < numOfLec; i++) {
                    JSONObject tmp = (JSONObject)lectureInfoArray.get(i);
                    lectureNum[i] = tmp.getString("id_lectures");
                    lectureName[i] = tmp.getString("name_lectures");
                    startTime[i] = tmp.getString("start");
                    endTime[i] = tmp.getString("end");

                    Log.d("lecnum",lectureNum[i]);
                    Log.d("name", lectureName[i]);
                    Log.d("time1", startTime[i]);
                    Log.d("time2", endTime[i]);
                }



                ProfLectureActivity.CustomList adapter = new ProfLectureActivity.CustomList(ProfLectureActivity.this);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(ProfLectureActivity.this, ProfStudentActivity.class);
                        intent.putExtra("lectureNum", lectureNum[position]);
                        intent.putExtra("lectureName", lectureName[position]);
                        startActivity(intent);
                    }
                });
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


}

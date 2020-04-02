package com.example.a2020limhwang;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;


public class ProfStateActivity extends AppCompatActivity{
    ImageButton back, profile, btn_edit;
    ListView listView;
    TextView text_lectureName;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    String lecture, id_students, str_result, lectureName, studentID, studentName;
    String[] date, state;
    int numOfState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profstate);

        Intent intent = getIntent();
        lecture = intent.getExtras().getString("lectureNum");
        lectureName = intent.getExtras().getString("lectureName");
        studentID = intent.getStringExtra("studentID");
        studentName = intent.getStringExtra("studentName");

        back = findViewById(R.id.back);
        profile = findViewById(R.id.profile);
        btn_edit = findViewById(R.id.btn_edit);
        listView= findViewById(R.id.listState);
        text_lectureName = findViewById(R.id.textView_lectureName);

        text_lectureName.setText(lectureName);

        sharedPreferences = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        id_students = sharedPreferences.getString("id_students", "");

        //ip고치기
        new ProfStateActivity.JSONTask().execute("http://192.168.0.76:3000/attendances/get");

    }
    public class CustomList extends ArrayAdapter<String> {
        private final Activity context;
        public CustomList(Activity context ) {
            super(context, R.layout.item_profstate, date);
            this.context = context;
        }
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView= inflater.inflate(R.layout.item_profstate, null, true);
            TextView tv_date = rowView.findViewById(R.id.date);
            TextView tv_state = rowView.findViewById(R.id.state);

            tv_date.setText(date[position]);
            tv_state.setText(state[position]);

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
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("id_students", studentID);
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
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    return buffer.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
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
            str_result = result + "";
            try {
                JSONObject jsonObject = new JSONObject(str_result);
                JSONArray stateInfoArray = jsonObject.getJSONArray("data");
                numOfState = stateInfoArray.length();

                date = new String[numOfState];
                state = new String[numOfState];

                for (int i = 0; i < numOfState; i++) {
                    JSONObject tmp = (JSONObject) stateInfoArray.get(i);
                    date[i] = tmp.getString("date");
                    state[i] = tmp.getString("state");

                    Log.d("date", date[i]);
                    Log.d("state", state[i]);

                    if(Integer.parseInt(state[i]) == 0){
                        state[i] = "미정";
                    }
                    else if(Integer.parseInt(state[i]) == 1){
                        state[i] = "출석";
                    }
                    else if(Integer.parseInt(state[i]) == 2){
                        state[i] = "지각";
                    }
                    else if(Integer.parseInt(state[i])== 3){
                        state[i] = "결석";
                    }

                }

                CustomList adapter = new CustomList(ProfStateActivity.this);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

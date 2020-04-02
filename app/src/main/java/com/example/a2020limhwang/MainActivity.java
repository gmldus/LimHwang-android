package com.example.a2020limhwang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static com.example.a2020limhwang.BeaconService.MY_PERMISSIONS;

public class MainActivity extends AppCompatActivity {

    Button loginButton;
    EditText id, pw;
    RadioButton radio_prof, radio_stud;
    String str_id, str_pw, str_result;
    ArrayList<String> studentKeyList = new ArrayList<>();
    ArrayList<String> studentValueList = new ArrayList<>();
    ArrayList<String> profKeyList = new ArrayList<>();
    ArrayList<String> profValueList = new ArrayList<>();
    String[] lectureNum;

    private SharedPreferences stud_sharedPreferences, prof_sharedPreferences;
    private SharedPreferences.Editor stud_editor, prof_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS);
            }
        } else { }

        id = findViewById(R.id.id);
        pw = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        radio_prof = findViewById(R.id.radio_prof);
        radio_stud = findViewById(R.id.radio_stud);

        stud_sharedPreferences = getSharedPreferences("sFile", MODE_PRIVATE);
        stud_editor = stud_sharedPreferences.edit();
        prof_sharedPreferences = getSharedPreferences("pFile", MODE_PRIVATE);
        prof_editor = prof_sharedPreferences.edit();

        Log.d("MainActivity", "id_students: " + stud_sharedPreferences.getString("id_students", null));
        Log.d("MainActivity", "id_professors: " + prof_sharedPreferences.getString("id_professors", null));

        if(stud_sharedPreferences.getString("id_students",null)!=null){
            //ip고치기
            new JSONTask().execute("http://192.168.0.76:3000/students/login");
        }
        else if(prof_sharedPreferences.getString("id_professors",null)!=null){
            //ip고치기
            new JSONTask().execute("http://192.168.0.76:3000/professors/get");
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str_id = id.getText().toString();
                str_pw = pw.getText().toString();

                //ip고치기
                if (radio_stud.isChecked()) {
                    stud_editor.commit();
                    new JSONTask().execute("http://192.168.0.76:3000/students/login");
                }
                else if (radio_prof.isChecked()) {
                    prof_editor.commit();
                    new JSONTask().execute("http://192.168.0.76:3000/professors/get");
                }
                else {
                    Toast.makeText(getApplicationContext(), "소속을 선택하세요", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public class JSONTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("login_id", str_id);
                jsonObject.accumulate("login_password", str_pw);

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
                    //login fail here
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
            if (result == null) {
                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
            }
            else {
                if(radio_stud.isChecked()) {
                    super.onPostExecute(result);

                    str_result = result+"";
                    try{
                        JSONObject jsonObject = new JSONObject(str_result);
                        String studentValue = jsonObject.getString("studentInfo");
                        JSONObject studentInfoObject = new JSONObject(studentValue);
                        JSONArray lectureInfoArray = jsonObject.getJSONArray("lectureNum");

                        Iterator i = studentInfoObject.keys();
                        while(i.hasNext()) {
                            String b = i.next().toString();
                            studentKeyList.add(b);
                        }
                        for (int j = 0; j < studentKeyList.size(); j++) {
                            studentValueList.add(studentInfoObject.getString(studentKeyList.get(j)));
                            stud_editor.putString(studentKeyList.get(j),studentValueList.get(j));
                            Log.d("key",studentKeyList.get(j));
                            Log.d("value",studentValueList.get(j));
                        }
                        stud_editor.commit();

                        lectureNum = new String[lectureInfoArray.length()];
                        for(int j = 0; j < lectureInfoArray.length(); j++){
                            JSONObject tmp = (JSONObject)lectureInfoArray.get(j);
                            String id = tmp.getString("id_lectures");
                            Log.d("lecnum",id);
                            lectureNum[j] = id;
                        }
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(MainActivity.this, ListActivity.class);
                    intent.putExtra("lectureNum", lectureNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else if (radio_prof.isChecked()) {
                    super.onPostExecute(result);

                    str_result = result+"";
                    try{
                        JSONObject jsonObject = new JSONObject(str_result);
                        JSONObject profInfo = (JSONObject) jsonObject.get("professorInfo");
                        JSONArray profLecInfo = (JSONArray) jsonObject.get("professorLectureInfo");

                        Iterator i = profInfo.keys();
                        while(i.hasNext()) {
                            String b = i.next().toString();
                            profKeyList.add(b);
                        }
                        for (int j = 0; j < profKeyList.size(); j++) {
                            profValueList.add(profInfo.getString(profKeyList.get(j)));
                            prof_editor.putString(profKeyList.get(j),profValueList.get(j));
                            Log.d("key",profKeyList.get(j));
                            Log.d("value",profValueList.get(j));
                        }
                        prof_editor.commit();

                        lectureNum = new String[profLecInfo.length()];
                        for(int j = 0; j < profLecInfo.length(); j++){
                            JSONObject tmp = (JSONObject)profLecInfo.get(j);
                            String id = tmp.getString("id_lectures");
                            Log.d("lecnum",id);
                            lectureNum[j] = id;
                        }
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(MainActivity.this, ProfLectureActivity.class);
                    intent.putExtra("lectureNum", lectureNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }
}

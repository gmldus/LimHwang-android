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
    String str_id, str_pw, str_result;
    ArrayList<String> studentKeyList = new ArrayList<>();
    ArrayList<String> studentValueList = new ArrayList<>();
    String[] lectureNum;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

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
        sharedPreferences = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if(sharedPreferences.getString("id_students",null)!=null){
            new JSONTask().execute("http://172.30.1.23:3000/students/login");
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str_id = id.getText().toString();
                str_pw = pw.getText().toString();
                editor.putString("id",str_id);
                editor.putString("pw",str_pw);
                editor.commit();
                //Log.d("wdwdwdwdwdwdwdw",sharedPreferences.getString("id",null));
                new JSONTask().execute("http://172.30.1.23:3000/students/login");
            }
        });
    }

    public class JSONTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("login_id", sharedPreferences.getString("id",null));
                jsonObject.accumulate("login_password", sharedPreferences.getString("pw",null));

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
                        editor.putString(studentKeyList.get(j),studentValueList.get(j));
                        Log.d("key",studentKeyList.get(j));
                        Log.d("value",studentValueList.get(j));
                    }


                    editor.commit();

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
                startActivity(intent);
            }
        }
    }
}

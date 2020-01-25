package com.example.a2020limhwang;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    Button loginButton;
    EditText id, pw;
    String str_id, str_pw, str_result;
    ArrayList<String> studentKeyList = new ArrayList<>();
    ArrayList<String> studentValueList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        id = findViewById(R.id.id);
        pw = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        sharedPreferences = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str_id = id.getText().toString();
                str_pw = pw.getText().toString();
                new JSONTask().execute("http://192.168.25.31:3000/students/login");
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
                    Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

            str_result = result+"";
            try{
                JSONObject jsonObject = new JSONObject(str_result);
                String studentValue = jsonObject.getString("studentInfo");
                JSONObject studentInfoObject = new JSONObject(studentValue);
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
            }catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(MainActivity.this, ListActivity.class);
            startActivity(intent);
        }

    }
}

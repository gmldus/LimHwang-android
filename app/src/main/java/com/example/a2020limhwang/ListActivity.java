package com.example.a2020limhwang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.example.a2020limhwang.BeaconService.MY_PERMISSIONS;

public class ListActivity extends AppCompatActivity {

    Button back, profile;
    ListView listView;

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
    private String str_result;
    int numOfLec;
    String[] lectureNum, name, time1, time2, beaconID;
    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Intent intent = getIntent();
        lectureNum = intent.getStringArrayExtra("lectureNum");

        back = findViewById(R.id.back);
        profile = findViewById(R.id.profile);
        listView= findViewById(R.id.listView);

        new JSONTask().execute("http://192.168.0.43:3000/lectures/get");

        beaconManager = BeaconManager.getInstanceForApplication(this);

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

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        Intent A = new Intent(ListActivity.this,BeaconService.class);
        startService(A);

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
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            str_result = result+"";
            try{
                JSONObject jsonObject = new JSONObject(str_result);
                JSONArray lectureInfoArray = jsonObject.getJSONArray("lectureList");
                numOfLec = lectureInfoArray.length();

                lectureNum =new String[numOfLec];
                name= new String[numOfLec];
                time1 = new String[numOfLec];
                time2 = new String[numOfLec];
                beaconID = new String[numOfLec];

                for (int i = 0; i < numOfLec; i++) {
                    JSONObject tmp = (JSONObject)lectureInfoArray.get(i);
                    lectureNum[i] = tmp.getString("id_lectures");
                    name[i] = tmp.getString("name_lectures");
                    time1[i] = tmp.getString("start");
                    time2[i] = tmp.getString("end");
                    beaconID[i] = tmp.getString("id_beacon");
                    Log.d("lecnum",lectureNum[i]);
                    Log.d("name", name[i]);
                    Log.d("time1", time1[i]);
                    Log.d("time2", time2[i]);
                    Log.d("beaconID", beaconID[i]);
                }

                CustomList adapter = new CustomList(ListActivity.this);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                        intent.putExtra("lectureNum", lectureNum[position]);
                        startActivity(intent);
                    }
                });
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}

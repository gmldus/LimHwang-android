package com.example.a2020limhwang;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BeaconService extends Service {
    public static final String TAG = "BeaconsEverywhere";
    public static final int MY_PERMISSIONS = 1;
    private BeaconManager beaconManager;
    private List<Beacon> beaconList = new ArrayList<>();
    BeaconService.MyTimer myTimer;
    int timerState = 0;
    int attState, num = 0;
    long now;
    Date dateNow;
    SimpleDateFormat dateFormat, allFormat;
    String timeNow;
    String start, end;
    String start_text, end_text;
    Date dateStart, dateEnd;
    Region region;
    String[] startTime, endTime, beaconID;

    public IBinder onBind(Intent intent){
        return null;
    }
    public void onCreate(){

        Log.d("Service","onStart()");
        super.onCreate();
        String channelId = "com.codechacha.sample1";

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Android Test",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

        }
        Notification noti = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("hello")
                .setContentText("this is beacon").build();

        startForeground(1, noti);
        handler.sendEmptyMessage(0);
    }
    public void onDestroy(){
        super.onDestroy();
        Log.d("Service","onDestroyed()");
    }

    public int onStartCommand(Intent intent, int flags, int startId){

        num = intent.getIntExtra("numOfLec",0);

        startTime = new String[num];
        endTime = new String[num];
        beaconID = new String[num];

        startTime = intent.getStringArrayExtra("startTime");
        endTime = intent.getStringArrayExtra("endTime");
        beaconID = intent.getStringArrayExtra("beaconID");

        beaconManager = BeaconManager.getInstanceForApplication(this);
        myTimer = new BeaconService.MyTimer(20000, 1000);

        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    Log.d(TAG, "didEnterRegion");
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "didExitRegion");
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                        if (beacon.getDistance() < 1.0) {
                            Log.d(TAG, "I see a beacon that is less than 1 meters away.");
                            beaconList.add(beacon);
                            timerState = 1;
                            myTimer.cancel();
                        }
                        else{
                            Log.d(TAG, "no beacon in range");
                            if (timerState == 1) {
                                myTimer.start();
                                timerState = 0;
                            }
                        }
                    }
                }
            }
        });
        handler.sendEmptyMessage(0);
        //String s = Integer.toString(beaconList.size());

        return super.onStartCommand(intent, flags, startId);
    }
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String s = Integer.toString(beaconList.size());
            //textView.setText(s);
            now = System.currentTimeMillis();
            dateNow = new Date(now);
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            allFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            timeNow = dateFormat.format(dateNow);
            //log
            Log.d("log now", timeNow);
            Log.d("log dateNow", dateNow.toString());
            Log.d("timeNow", timeNow);

            //여기서 start와 end값 한쌍 채택 (가까운 범위안에 드는 애들로)
            start = "2020-02-13 21:37:00";
            end = "2020-02-13 22:50:00";
            Date currentTime = Calendar.getInstance().getTime();
            String date_text = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime);
            //date_text=date_text.concat(" "+startTime[0]);
            Log.d("webnautes", date_text);

            try {
                for (int i = 0;i<num; i++){
                    start_text = date_text.concat(" "+startTime[i]);
                    end_text = date_text.concat(" "+endTime[i]);
                    dateStart = allFormat.parse(start_text);
                    dateEnd = allFormat.parse(end_text);

                    //log
                    Log.d("log date start", dateStart.toString());
                    Log.d("log date end", dateEnd.toString());

                    long gapStart = dateNow.getTime() - dateStart.getTime();
                    long gapEnd = dateNow.getTime() - dateEnd.getTime();

                    gapStart /= 60000;
                    gapEnd /= 60000;

                    //log
                    Log.d("log dateNow.getTime()", Long.toString(dateNow.getTime()));
                    Log.d("log dateStart.getTime()", Long.toString(dateStart.getTime()));
                    Log.d("log dateEnd.getTime()", Long.toString(dateEnd.getTime()));

                    Log.d("log now - start", Long.toString(dateNow.getTime() - dateStart.getTime()));
                    Log.d("log now - end", Long.toString(dateNow.getTime() - dateEnd.getTime()));
                    //log
                    Log.d("log gap start", Long.toString(gapStart));
                    Log.d("log gap end", Long.toString(gapEnd));

                    if (gapStart >= -10 && gapEnd <= 0) {
                        region = new Region("myBeacons", Identifier.parse("e2c56db5-dffb-48d2-b060-d0f5a71096e0"), Identifier.parse("30001"),Identifier.parse(beaconID[i]));
                        Log.d("vsdfedcscsefd", Identifier.parse(beaconID[i]).toString());
                        if (attState == 0 && gapStart > 10 && gapEnd < -10) {

                            Log.d("if", "1");
                            attState = 2;
                            //textView3.setText("2. 지각");
                            try {
                                Log.d("start", "지각 start");
                                beaconManager.startMonitoringBeaconsInRegion(region);
                                beaconManager.startRangingBeaconsInRegion(region);

                            } catch (RemoteException e) {
                            }
                        } else if (attState == 0 && gapStart >= -10 && gapStart <= 10) {
                            Log.d("if", "2");
                            attState = 1;
                            //textView3.setText("1. 출석");
                            try {
                                beaconManager.startMonitoringBeaconsInRegion(region);
                                beaconManager.startRangingBeaconsInRegion(region);
                                Log.d("start", "출석 start");
                            } catch (RemoteException e) {
                            }
                        } else if (attState == 0 && gapEnd >= -10 && gapEnd < 0) {
                            Log.d("if", "결석 3");
                            attState = 3;
                            //textView3.setText("3. 결석");
                        } else if(gapEnd == 0){  //initialize
                            //new JSONTask().execute("http://192.168.0.43:3000/attendances/update");
                            attState = 0;
                            timerState = 0;
                        }
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            for (Beacon beacon : beaconList) {
                //textView.setText("ID : " + beacon.getId1() + " / " + "Distance : " + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n");
            }

            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    class MyTimer extends CountDownTimer
    {
        public MyTimer(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            //textView2.setText(millisUntilFinished/1000 + " 초");
            Log.d("timer", millisUntilFinished/1000 + " 초");
        }

        @Override
        public void onFinish() {
            //textView3.setText("결석");
            //결석처리
        }
    }

    public class JSONTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try{
                JSONObject jsonObject = new JSONObject();
                //jsonObject.accumulate("array", Arrays.toString(lectureNum));

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
            /*Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
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

                ListActivity.CustomList adapter = new ListActivity.CustomList(ListActivity.this);
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
            }*/
        }

    }

}

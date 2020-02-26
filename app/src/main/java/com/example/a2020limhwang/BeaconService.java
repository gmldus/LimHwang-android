package com.example.a2020limhwang;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
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
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    BeaconService.MyTimer myTimer;
    int timerState = 0;
    int attState, num = 0;
    long now;
    Date dateNow;
    SimpleDateFormat timeFormat, allFormat;
    String id_students;
    String timeNow, checkTime;
    String start, end;
    String start_text, end_text, date_text;
    Date dateStart, dateEnd, currentTime;
    Region region;
    String[] startTime, endTime, beaconID, lectureNum;
    int index, tmp;
    int postState = 0, isChecked = 0;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

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

        sharedPreferences = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        id_students = sharedPreferences.getString("id_students", "");

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
        lectureNum = intent.getStringArrayExtra("lectureNum");

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1500);
        myTimer = new BeaconService.MyTimer(600000, 1000);

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

                Log.d("beaconsize", beacons.size()+"");
                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                                if (beacon.getDistance() < 1) {
                                    Log.d("비콘", "I see a beacon that is less than 1 meters away.");
                                    beaconList.add(beacon);
                                    timerState = 1;
                            myTimer.cancel();
                            if (tmp == 1) {
                                attState = 1;
                                Log.d("attState(set)", attState+"");
                            }
                            else if (tmp == 2) {
                                attState = 2;
                                Log.d("attState(set)", attState+"");
                            }
                        }
                        else{
                            Log.d("비콘", "no beacon in range");
                            if (timerState == 1) {
                                myTimer.start();
                                timerState = 0;
                            }
                        }
                    }
                }

            }
        });
        handler.sendEmptyMessageDelayed(0, 10000);
        //String s = Integer.toString(beaconList.size());

        return super.onStartCommand(intent, flags, startId);
    }
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            now = System.currentTimeMillis();
            dateNow = new Date(now);
            timeFormat = new SimpleDateFormat("HH:mm:ss");
            allFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            timeNow = timeFormat.format(dateNow);

            start = "2020-02-13 21:37:00";
            end = "2020-02-13 22:50:00";
            currentTime = Calendar.getInstance().getTime();
            date_text = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime);
            //date_text=date_text.concat(" "+startTime[0]);
            Log.d("webnautes", date_text);
            powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);


            wakeLock  = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK  |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE, "My:Tag");
            String channelId2 = "channel2";
            String channelName2 = "Channel Name2";

            NotificationManager notifManager2

                    = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel mChannel2 = new NotificationChannel(
                        channelId2, channelName2, importance);

                notifManager2.createNotificationChannel(mChannel2);

            }

            NotificationCompat.Builder builder2 =
                    new NotificationCompat.Builder(getApplicationContext(), channelId2);

            Intent notificationIntent = new Intent(getApplicationContext()

                    , ListActivity.class);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

            builder2.setContentTitle("출석 검사 중 입니다") // required
                    .setContentText("기다려주세요")  // required
                    .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                    .setAutoCancel(true) // 알림 터치시 반응 후 삭제
                    .setSmallIcon(android.R.drawable.btn_star)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            try {
                for (int i = 0;i<num; i++){
                        start_text = date_text.concat(" "+startTime[i]);
                        end_text = date_text.concat(" "+endTime[i]);
                        dateStart = allFormat.parse(start_text);
                        dateEnd = allFormat.parse(end_text);

                        //log
                        //Log.d("log date start", dateStart.toString());
                        //Log.d("log date end", dateEnd.toString());

                        long gapStart = dateNow.getTime() - dateStart.getTime();
                        long gapEnd = dateNow.getTime() - dateEnd.getTime();

                        gapStart /= 60000;
                        gapEnd /= 60000;

                        if (gapStart >= 0 && gapEnd <= 10) {
                        index = i;
                        if (isChecked == 0) {
                            isChecked = 1;
                            checkTime = timeNow;
                        }

                        //log
                        //Log.d("log dateNow.getTime()", Long.toString(dateNow.getTime()));
                        //Log.d("log dateStart.getTime()", Long.toString(dateStart.getTime()));
                        //Log.d("log dateEnd.getTime()", Long.toString(dateEnd.getTime()));

                        //Log.d("log now - start", Long.toString(dateNow.getTime() - dateStart.getTime()));
                        //Log.d("log now - end", Long.toString(dateNow.getTime() - dateEnd.getTime()));
                        Log.d("log gap start", Long.toString(gapStart));
                        Log.d("log gap end", Long.toString(gapEnd));
                        Log.d("log attState", attState+"");
                        Log.d("log tmp", tmp+"");
                        region = new Region("myBeacons", Identifier.parse("e2c56db5-dffb-48d2-b060-d0f5a71096e0"), Identifier.parse("30001"),Identifier.parse(beaconID[i]));
                        Log.d("log region", Identifier.parse(beaconID[i]).toString());
                        if (attState == 0 && gapStart >= 0 && gapStart < 1) {
                            try {
                                tmp = 1;
                                beaconManager.startMonitoringBeaconsInRegion(region);
                                beaconManager.startRangingBeaconsInRegion(region);
                                Log.d("now", "출석 start");
                            } catch (RemoteException e) {
                            }
                        }
                        else if(gapStart==1) {

                            try{
                                wakeLock.acquire();
                                notifManager2.notify(0, builder2.build());

                                tmp = 1;
                                beaconManager.startMonitoringBeaconsInRegion(region);
                                beaconManager.startRangingBeaconsInRegion(region);

                                wakeLock.release();
                            } catch (RemoteException e) {
                            }
                        }
                        else if (attState == 0 && gapStart > 1 && gapEnd < -1) {
                            try {
                                tmp = 2;
                                beaconManager.startMonitoringBeaconsInRegion(region);
                                beaconManager.startRangingBeaconsInRegion(region);
                                Log.d("now", "지각 start");

                            } catch (RemoteException e) {
                            }
                        }
                        else if (attState == 0 && gapEnd >= -1 && gapEnd < 0) {
                            Log.d("now", "결석 start");
                            tmp = 3;
                        }
                        else if(gapEnd == 0){  //initialize
                            if (postState == 0) {
                                postState = 1;
                                if (attState == 0 && tmp == 3) {
                                    Log.d("attState(if)", attState+"");
                                    attState = 3;
                                    checkTime = "00:00:00";
                                }
                                //ip고치기
                                Log.d("attState", attState+"");
                                new JSONTask().execute("http://172.30.1.59:3000/attendances/update");
                            }
                        }
                        else if (gapEnd == 1) {
                            postState = 0;

                        }
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            handler.sendEmptyMessageDelayed(0, 10000);
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
            attState=3;
            //결석처리
        }
    }

    public class JSONTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try{
                JSONObject jsonObject = new JSONObject();
                Log.d("id_students", id_students);
                Log.d("id_lectures", lectureNum[index]);
                Log.d("date", date_text.substring(0, 10));
                Log.d("state", attState+"");
                jsonObject.accumulate("id_students", Integer.parseInt(id_students));
                jsonObject.accumulate("id_lectures", Integer.parseInt(lectureNum[index]));
                jsonObject.accumulate("date", date_text.substring(0, 10));
                jsonObject.accumulate("state", attState);
                jsonObject.accumulate("check_time", checkTime);

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


            powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);

            wakeLock  = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK  |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE, "My:Tag");

            super.onPostExecute(result);

            String channelId = "channel";
            String channelName = "Channel Name";

            NotificationManager notifManager

                    = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel mChannel = new NotificationChannel(
                        channelId, channelName, importance);

                notifManager.createNotificationChannel(mChannel);

            }

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getApplicationContext(), channelId);

            Intent notificationIntent = new Intent(getApplicationContext()

                    , MainActivity.class);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

            String str_result = result+"";
            try{
                JSONObject jsonObject = new JSONObject(str_result);

                if(attState==3 ){

                    if(sharedPreferences.getInt(sharedPreferences.getString(lectureNum[index],null),50)-1 < 0)
                        editor.putInt(sharedPreferences.getString(lectureNum[index],null),0);
                    else editor.putInt(sharedPreferences.getString(lectureNum[index],null),sharedPreferences.getInt(sharedPreferences.getString(lectureNum[index],null),50)-1);
                    editor.commit();
                    builder.setContentTitle(sharedPreferences.getString(lectureNum[index],null)+" 결석입니다") // required
                            .setContentText("F까지 결석횟수 "+sharedPreferences.getInt(sharedPreferences.getString(lectureNum[index],null),50)+"번 남았습니다")  // required
                            .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                            .setAutoCancel(true) // 알림 터치시 반응 후 삭제
                            .setSmallIcon(android.R.drawable.btn_star)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                    wakeLock.acquire();
                    notifManager.notify(0, builder.build());
                    wakeLock.release();
                }


            }catch (JSONException e) {
                e.printStackTrace();
            }

            attState = 0;
            timerState = 0;
            isChecked = 0;
        }

    }

}

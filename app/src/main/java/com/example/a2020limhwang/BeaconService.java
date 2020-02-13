package com.example.a2020limhwang;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class BeaconService extends Service {
    public static final String TAG = "BeaconsEverywhere";
    public static final int MY_PERMISSIONS = 1;
    private BeaconManager beaconManager;
    private List<Beacon> beaconList = new ArrayList<>();
    BeaconService.MyTimer myTimer;
    int timerState = 0;
    int attState = 0;
    long now;
    Date dateNow;
    SimpleDateFormat dateFormat, allFormat;
    String timeNow;
    String start, end;
    Date dateStart, dateEnd;
    final Region region = new Region("myBeacons", Identifier.parse("e2c56db5-dffb-48d2-b060-d0f5a71096e0"), null,null);

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
    }
    public void onDestroy(){
        super.onDestroy();
        Log.d("Service","onDestroyed()");
    }

    public int onStartCommand(Intent intent, int flags, int startId){

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

        String s = Integer.toString(beaconList.size());

        now = System.currentTimeMillis();
        dateNow = new Date(now);
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        allFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        timeNow = dateFormat.format(dateNow);
        //log
        Log.d("log now", timeNow);
        Log.d("log dateNow", dateNow.toString());
        Log.d("timeNow", timeNow);
        start = "2020-02-13 19:50:00";
        end = "2020-02-13 20:50:00";

        try {
            dateStart = allFormat.parse(start);
            dateEnd = allFormat.parse(end);
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

            if (gapStart >= -10 && gapEnd <=0) {
                if (attState == 0 && gapStart > 10 && gapEnd < -10) {
                    Log.d("if", "1");
                    attState = 2;
                    //textView3.setText("2. 지각");
                    try {
                        Log.d("start", "지각 start");
                        beaconManager.startRangingBeaconsInRegion(region);
                        beaconManager.startMonitoringBeaconsInRegion(region);
                    } catch (RemoteException e) { }
                }
                else if (attState == 0 && gapStart >= -10 && gapStart <= 10) {
                    Log.d("if", "2");
                    attState = 1;
                    //textView3.setText("1. 출석");
                    try {
                        beaconManager.startRangingBeaconsInRegion(region);
                        beaconManager.startMonitoringBeaconsInRegion(region);
                        Log.d("start", "출석 start");
                    } catch (RemoteException e) { }
                }
                else if(attState == 0 && gapEnd >= -10 && gapEnd <= 0) {
                    Log.d("if", "3");
                    attState = 3;
                    //textView3.setText("3. 결석");
                }
            }
        }catch (ParseException e){
            e.printStackTrace();
        }

        for(Beacon beacon : beaconList){
            Log.d("hihihihih","hkkkkkkkkkk");
            //textView.setText("ID : " + beacon.getId1() + " / " + "Distance : " + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n");
        }
        return super.onStartCommand(intent, flags, startId);
    }
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

}

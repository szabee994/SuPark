package com.awt.supark;

/**
 * Created by Szabolcs on 2015.11.30..
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ParkingTimerService extends Service {

    String licenseNum;
    int parkingLength;
    int parkingZone;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate(){
        super.onCreate();



        Log.i("Service", "Service created");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.i("Service", "Service started");
        /*
        String licenseNum = intent.getStringExtra("licenseNum");
        int parkingLength = intent.getIntExtra("parkingLength", 0);
        int parkingZone = intent.getIntExtra("parkingZone", -1);

        Log.i("asd", licenseNum + ", " + parkingLength + ", " + parkingZone);*/

        return START_STICKY;
                //super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.i("Service", "Service destroyed");
    }

}

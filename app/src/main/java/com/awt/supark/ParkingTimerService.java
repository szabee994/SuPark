package com.awt.supark;

/**
 * Created by Szabolcs on 2015.11.30..
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ParkingTimerService extends Service {
    SQLiteDatabase              db;
    Context                     context;
    Cursor                      d;
    int                         stopId;
    NotificationManager         notificationManager;
    NotificationCompat.Builder  mNotification;
    CountDownTimer[]            countTimer;
    Timer                       refreshTimer;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate(){
        super.onCreate();
        Log.i("Service", "---------- Service created ----------");
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.i("Service", "---------- Service started ----------");

        loadDatabase();
        startTimer();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.i("Service", "---------- Service destroyed ----------");
    }

    private boolean isThereAnyParkedCars() {
        Cursor cursor = db.rawQuery("SELECT * FROM cars WHERE parkedstate = 1", null);

        if(cursor.getCount() == 0) {
            return false;
        } else {
            return  true;
        }
    }

    public void startTimer() {
        if(refreshTimer == null) {
            refreshTimer = new Timer("refreshNotificaition");
            refreshTimer.schedule(_timerTask, 0, 2000);
        }
    }

    public void cancelTimer() {
        if(refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    private final TimerTask _timerTask = new TimerTask() {
        @Override
        public void run() {
            Log.i("Service", "* Updating DB...");
            requestCars();
        }
    };

    private void loadDatabase() {
        // Loading the database
        try {
            db = SQLiteDatabase.openDatabase(getApplicationContext().getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
            Log.i("Service", "DB loaded successfully");
        } catch (Exception e) {
            Log.i("Service", "DB read error");
            Log.i("Service", "Exception: " + e.toString());
        }
    }

    // ezt ciklikaljuk majd
    private void requestCars() {
        d = db.rawQuery("SELECT * FROM cars", null);

        // Fetching the results
        for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
            // Putting stuff into variables
            int carId = d.getInt(d.getColumnIndex("car_id"));
            String carLicense = d.getString(d.getColumnIndex("car_license"));
            long parkedTime = d.getLong(d.getColumnIndex("parkedtime"));
            long parkedUntil = d.getLong(d.getColumnIndex("parkeduntil"));
            int parkedState = d.getInt(d.getColumnIndex("parkedstate"));

            // Calculating remaining parking length
            final long remainingTime = parkedUntil - System.currentTimeMillis() / 1000L;

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, (int) remainingTime / 60);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm (MMM dd.)");
            final String endTime = sdf.format(calendar.getTime()); // Parking end in HH:mm (MMM dd.)

            Log.i("Service", "Car " + carId + " | License: " + carLicense + " | State: " + parkedState);

            if(parkedState == 1) {
                createNotification(carId, carLicense, endTime, remainingTime/60, parkedTime, parkedUntil);
            } else {
                removeNotification(carId);
            }

            //if (!isThereAnyParkedCars()) {
            //    cancelTimer();
            //}
        }
    }

    private void createNotification(final int id, final String licenseNum, final String formattedEndTime, final long remainingTime, final long parkedTime, final long parkedUntil) {
        try {
            // Building the notification
            mNotification = new NotificationCompat.Builder(getApplicationContext());
            mNotification.setSmallIcon(R.mipmap.ic_directions_car_white_24dp);
            //mNotification.setOngoing(true);
            mNotification.setContentTitle("Parking status of " + licenseNum);
            mNotification.setContentText(remainingTime + " minutes remained, ticket due: " + formattedEndTime);
            mNotification.setProgress((int) (parkedUntil - parkedTime), (int) remainingTime * 60, false);

            notificationManager.notify(id, mNotification.build());

            Log.i("Service", "Notification created for ID: " + id);
        } catch (Exception e) {
            Log.i("Service", "Failed to create notification for ID: " + id);
            Log.i("Service", "Exception: " + e.toString());
        }
    }

    private void removeNotification(final int id) {
        notificationManager.cancel(id);
    }

    private void finishNotification(final int id, final String licenseNum) {
        try {
            // Building the notification
            mNotification = new NotificationCompat.Builder(getApplicationContext());
            mNotification.setSmallIcon(R.mipmap.ic_directions_car_white_24dp);
            mNotification.setOngoing(false);
            mNotification.setContentTitle("Parking status of " + licenseNum);
            mNotification.setContentText("Parking ticket due!");
            mNotification.setProgress(0, 0, false);

            notificationManager.notify(id, mNotification.build());

            Log.i("Service", "Finish notification created for ID: " + id);
        } catch (Exception e) {
            Log.i("Service", "Failed to create notification for ID: " + id);
            Log.i("Service", "Exception: " + e.toString());
        }
    }
}

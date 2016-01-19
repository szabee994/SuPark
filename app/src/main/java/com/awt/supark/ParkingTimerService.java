package com.awt.supark;

/**
 * Created by Szabolcs on 2015.11.30..
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ParkingTimerService extends Service {
    private static final int TIMER_PERIOD = 2000;
    private static final String ACTION_CANCEL = "com.awt.supark.ParkingTimerService.ACTION_CANCEL";
    private static final String ACTION_RENEW = "com.awt.supark.ParkingTimerService.ACTION_RENEW";

    SQLiteDatabase              db;
    Context                     context;
    Cursor                      d;
    NotificationManager         notificationManager;
    NotificationCompat.Builder  mNotification;
    Timer                       refreshTimer;
    TimerTask                   timerTask;

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

        // Checking that are there any cars in the database. If the result is true starts the timer, otherwise destroys the service.
        if(isThereAnyCars()) {
            startTimer(TIMER_PERIOD);
        } else {
            this.stopService(intent);
        }

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
            cursor.close();
            return false;
        } else {
            cursor.close();
            return  true;
        }
    }

    private boolean isThereAnyCars() {
        Cursor cursor = db.rawQuery("SELECT * FROM cars", null);

        if(cursor.getCount() == 0) {
            cursor.close();
            return false;
        } else {
            cursor.close();
            return  true;
        }
    }

    public void startTimer(int period) {
        if(refreshTimer == null) {
            refreshTimer = new Timer("refreshNotification", true);
            timerTask = new _timerTask();
            refreshTimer.schedule(timerTask, 0, period);
            Log.i("Service", "Refresh timer started...");
        }
    }

    public void cancelTimer() {
        if(refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer.purge();
            refreshTimer = null;
        }
    }

    private void loadDatabase() {
        try {
            db = SQLiteDatabase.openDatabase(getApplicationContext().getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
            Log.i("Service", "DB loaded successfully...");
        } catch (Exception e) {
            Log.i("Service", "DB read error");
            Log.i("Service", "Exception: " + e.toString());
        }
    }

    /*
     * Reloads the car database and if there's a car where the parked state is true
     * creates a notification for it or refreshes the one that was already created.
     */
    private void requestCars() {
        d = db.rawQuery("SELECT * FROM cars", null);

        // Fetching the results
        for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {

            // Putting stuff into variables
            int carId = d.getInt(d.getColumnIndex("car_id"));
            String carLicense = d.getString(d.getColumnIndex("car_license"));
            long parkedTime =   d.getLong(d.getColumnIndex("parkedtime"));
            long parkedUntil =  d.getLong(d.getColumnIndex("parkeduntil"));
            int parkedState =   d.getInt(d.getColumnIndex("parkedstate"));

            // Calculating remaining parking length
            long remainingTime = (parkedUntil - System.currentTimeMillis() / 1000L);

            // Formatting the ending time
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, (int) remainingTime / 60);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm (MMM dd.)");
            final String endTime = sdf.format(calendar.getTime()); // Parking end in HH:mm (MMM dd.)

            Log.i("Service", "Car " + carId + " | License: " + carLicense + "\t | State: " + parkedState + " | Remaining: " + remainingTime);

            if(parkedState == 1) {
                if(remainingTime > 0) {
                    createNotification(carId, carLicense, endTime, (remainingTime / 60) + 1, parkedTime, parkedUntil);
                } else {
                    finishNotification(carId, carLicense);
                    removeNotification(carId);
                    stopPark(carId);
                }
            } else {
                removeNotification(carId);
            }
        }

        d.close();

        // If there is no parked cars cancels the timer and destroys the service
        if(!isThereAnyParkedCars()) {
            cancelTimer();
            Intent i = new Intent(this, ParkingTimerService.class);
            stopService(i);
        }
    }

    private void createNotification(final int id, final String licenseNum, final String formattedEndTime, final long remainingTime, final long parkedTime, final long parkedUntil) {
        try {
            // Cancel button action
            Intent cancelIntent = new Intent(ACTION_CANCEL);
            cancelIntent.putExtra("cancelId", id);
            PendingIntent pIntentCancel = PendingIntent.getBroadcast(this, id, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Renew button action
            Intent renewIntent = new Intent(ACTION_RENEW);
            renewIntent.putExtra("renewId", id);
            PendingIntent pIntentRenew = PendingIntent.getBroadcast(this, id, renewIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mNotification = new NotificationCompat.Builder(getApplicationContext());                                // Setting the notification parameters:
            mNotification.setSmallIcon(R.mipmap.ic_directions_car_white_24dp);                                      // * icon
            mNotification.setOngoing(true);                                                                         // * making it ongoing so the user can't swipe away
            mNotification.setContentTitle("Parking status of " + licenseNum.toUpperCase());                         // * title
            mNotification.setContentText(remainingTime + " minute(s) left, ticket due: " + formattedEndTime);       // * content text
            mNotification.setProgress((int) (parkedUntil - parkedTime), (int) remainingTime * 60, false);           // * progress bar to visualize the remaining time
            mNotification.addAction(R.mipmap.ic_highlight_off_white_24dp, "Cancel", pIntentCancel);                 // * cancel button
            mNotification.addAction(R.mipmap.ic_loop_white_24dp, "Renew", pIntentRenew);                            // * renew button
            notificationManager.notify(id, mNotification.build());                                                  // Finally we can build the actual notification where the ID is the selected car's ID

        } catch (Exception e) {
            Log.i("Service", "Failed to create notification for ID: " + id);
            Log.i("Service", "Exception: " + e.toString());
        }
    }

    private void removeNotification(final int id) {
        try {
            notificationManager.cancel(id);
            //Log.i("Service", "Notification successfully removed for ID: " + id);
        } catch (Exception e) {
            Log.i("Service", "Failed to remove notification for ID: " + id);
            Log.i("Service", "Exception: " + e.toString());
        }
    }

    private void finishNotification(final int id, final String licenseNum) {
        try {
            mNotification = new NotificationCompat.Builder(getApplicationContext());
            mNotification.setSmallIcon(R.mipmap.ic_report_problem_white_24dp);
            mNotification.setContentTitle("Parking status of " + licenseNum);
            mNotification.setContentText("Parking ticket due!");
            mNotification.setProgress(0, 0, false);

            /*
             * This time we have to use a different ID for the notification
             * because we don't have to updated it anymore.
             * The ID will be the current system time (in ms).
             */
            notificationManager.notify((int) System.currentTimeMillis(), mNotification.build());

            //Log.i("Service", "Finish notification created for ID: " + id);
        } catch (Exception e) {
            Log.i("Service", "Failed to create notification for ID: " + id);
            Log.i("Service", "Exception: " + e.toString());
        }
    }

    private void stopPark(final int id) {
        Log.i("Service", "Parking stop requested for: " + id);

        ContentValues temp = new ContentValues();
        temp.put("parkedstate", 0);
        db.update("cars", temp, "car_id = " + id, null);
    }

    // Broadcast receiver for handling the actions from notification
    public static class MyReceiver extends BroadcastReceiver {
        SQLiteDatabase db;
        private Context context;

        @Override
        public void onReceive(Context context, Intent intent) {
            this.context = context;

            if (intent.getAction().equals(ACTION_CANCEL)) {
                Log.i("Service", "Cancel broadcast received");

                int cancelId = intent.getIntExtra("cancelId", 0);
                loadDatabase();
                stopPark(cancelId);
            } else if (intent.getAction().equals(ACTION_RENEW)) {
                Log.i("Service", "Renew broadcast received");
            }
        }

        private void loadDatabase() {
            try {
                db = SQLiteDatabase.openDatabase(context.getApplicationContext().getFilesDir().getPath() +
                        "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
                Log.i("Service", "DB loaded successfully...");
            } catch (Exception e) {
                Log.i("Service", "DB read error");
                Log.i("Service", "Exception: " + e.toString());
            }
        }

        private void stopPark(final int id) {
            Log.i("Service", "Parking stop requested for: " + id);

            ContentValues temp = new ContentValues();
            temp.put("parkedstate", 0);
            db.update("cars", temp, "car_id = " + id, null);
        }
    }

    private class _timerTask extends TimerTask {
        @Override
        public void run() {
            Log.i("Service", "Updating DB...");
            requestCars();
        }
    }
}

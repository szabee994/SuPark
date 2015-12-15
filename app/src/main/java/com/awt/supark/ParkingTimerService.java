package com.awt.supark;

/**
 * Created by Szabolcs on 2015.11.30..
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ParkingTimerService extends Service {
    private static final int TIMER_PERIOD = 2000;

    SQLiteDatabase              db;
    Context                     context;
    Cursor                      d;
    NotificationManager         notificationManager;
    NotificationCompat.Builder  mNotification;
    Timer                       refreshTimer;
    TimerTask                   timerTask;
    BroadcastReceiver           mReceiver;

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

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Service", "Cancel broadcast received.");

            Bundle answerBundle = intent.getExtras();
            int cancelId = answerBundle.getInt("cancelId");

            stopPark(cancelId);
        }

        // constructor
        public MyReceiver(){

        }
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

    /*
     * Starts a timer which will auto update the database and the notifications
     */
    public void startTimer(int period) {
        if(refreshTimer == null) {
            refreshTimer = new Timer("refreshNotification", true);
            timerTask = new _timerTask();
            refreshTimer.schedule(timerTask, 0, period);
            Log.i("Service", "Refresh timer started...");
        }
    }

    /*
     * Cancels the timer task
     */
    public void cancelTimer() {
        if(refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer.purge();
            refreshTimer = null;
        }
    }

    /*
     * Timer runs this command
     */
    private class _timerTask extends TimerTask {
        @Override
        public void run() {
            Log.i("Service", "Updating DB...");
            requestCars();
        }
    }

    /*
     * Initializes the database
     */
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

    /*
     * Creates or updates a notification
     */
    private void createNotification(final int id, final String licenseNum, final String formattedEndTime, final long remainingTime, final long parkedTime, final long parkedUntil) {
        /*Cancel intent
        Intent cancelIntent = new Intent(this, sheit);
        Bundle cancelBundle = new Bundle();
        cancelBundle.putInt("cancelId", id);
        cancelIntent.putExtras(cancelBundle);
        PendingIntent pIntentCancel = PendingIntent.getActivity(this, id, cancelIntent, 0); */

        try {
            mNotification = new NotificationCompat.Builder(getApplicationContext());                                // Setting the notification parameters:
            mNotification.setSmallIcon(R.mipmap.ic_directions_car_white_24dp);                                      // * icon
            mNotification.setOngoing(true);                                                                         // * making it ongoing so the user can't swipe away
            mNotification.setContentTitle("Parking status of " + licenseNum);                                       // * title
            mNotification.setContentText(remainingTime + " minute(s) left, ticket due: " + formattedEndTime);   // * content text
            mNotification.setProgress((int) (parkedUntil - parkedTime), (int) remainingTime * 60, false);           // * progress bar to visualize the remaining time
            //mNotification.addAction(R.mipmap.ic_remove_circle_outline_black_48dp, "Cancel", pIntentCancel);         // * cancel button
            notificationManager.notify(id, mNotification.build());                                                  // Finally we can build the actual notification where the ID is the selected car's ID

            //Log.i("Service", "Notification created for ID: " + id);
        } catch (Exception e) {
            Log.i("Service", "Failed to create notification for ID: " + id);
            Log.i("Service", "Exception: " + e.toString());
        }
    }

    /*
     * Removes the notification on the ID given in the parameter
     */
    private void removeNotification(final int id) {
        try {
            notificationManager.cancel(id);
            //Log.i("Service", "Notification successfully removed for ID: " + id);
        } catch (Exception e) {
            Log.i("Service", "Failed to remove notification for ID: " + id);
            Log.i("Service", "Exception: " + e.toString());
        }
    }

    /*
     * Displays a "parking due" notification
     */
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

    /*
     * Changes the parked state to false of the car found at the given ID
     */
    private void stopPark(final int id) {
        Log.i("Service", "Parking stop requested for: " + id);

        ContentValues temp = new ContentValues();
        temp.put("parkedstate", 0);
        db.update("cars", temp, "car_id = " + id, null);
    }
}

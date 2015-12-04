package com.awt.supark;

/**
 * Created by Szabolcs on 2015.11.30..
 */

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

import com.awt.supark.Adapter.CarListAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ParkingTimerService extends Service {

    Handler notificationHandler;
    Intent intent;
    PendingIntent pIntent;
    NotificationCompat.Builder mNotification;
    NotificationManager notificationManager;
    SQLiteDatabase db;
    Context context;
    Cursor d;

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

        db = SQLiteDatabase.openDatabase(getApplicationContext().getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        d = db.rawQuery("SELECT * FROM cars", null);

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

        for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
            int car_id = d.getInt(d.getColumnIndex("car_id"));
            String car_license = d.getString(d.getColumnIndex("car_license"));
            long parked_time = d.getLong(d.getColumnIndex("parkedtime"));
            long parked_until = d.getLong(d.getColumnIndex("parkeduntil"));
            int parkedstate = d.getInt(d.getColumnIndex("parkedstate"));

            Log.i("Car", "ID: " + car_id + ", Lic: " + car_license + ", Time: " + parked_time + ", Until: " + parked_until + ", State: " + parkedstate);

            if (parkedstate == 1) {
                createNotification(car_id, car_license, parked_until, parked_time);
            } else {
                try {
                    notificationManager.cancel(car_id);
                } catch (Exception e) {
                    Log.i("E", e.toString());
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.i("Service", "Service destroyed");
    }

    public void createNotification(final int id, final String licenseNum, final long parkedUntil, final long parkedTime, final int parkedState) {
        final long parkingLength = parkedUntil - System.currentTimeMillis() / 1000L;

        // Calculating parking time end
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, (int) parkingLength / 60);

        // Formatting the result
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm (MMM dd.)");
        final String endTime = sdf.format(calendar.getTime());


        // Intent of main activity
        //intent = new Intent(context, MainActivity.class);
        //pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Building the notification
        mNotification = new NotificationCompat.Builder(getApplicationContext());

        mNotification.setSmallIcon(R.mipmap.ic_directions_car_white_24dp);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        new CountDownTimer(parkingLength * 1000, 5000) {
                            public void onTick(long millisUntilFinished) {
                                Log.i("TICK!", "Time remaining: " + millisUntilFinished / 60000 + " min");
                                mNotification.setContentTitle("Parking status of " + licenseNum);
                                mNotification.setContentText(millisUntilFinished / 60000 + " minutes remained, ticket due: " + endTime);
                                mNotification.setProgress((int) (parkedUntil - parkedTime), (int) ((parkedUntil - parkedTime) - ((System.currentTimeMillis() / 1000) - parkedTime)), false);
                                Log.i("Int nezes", Integer.toString((int) parkedUntil) + " " + Integer.toString((int) (System.currentTimeMillis() / 1000)));
                                notificationManager.notify(id, mNotification.build());
                            }

                            public void onFinish() {
                                ContentValues temp = new ContentValues();
                                temp.put("parkedstate", 0);
                                db.update("cars", temp, "car_id = " + id, null);
                                db.close();

                                mNotification.setContentTitle("Parking status of " + licenseNum);
                                mNotification.setContentText("Parking ticket due!");
                                mNotification.setProgress(0, 0, false);
                                notificationManager.notify(id, mNotification.build());
                            }
                        }.start();
                    }
                }
        ).run();

        // Making it ongoing
        //mNotification.flags = Notification.FLAG_ONGOING_EVENT;


        //notificationManager.notify(id, mNotification);
    }

    public void deleteNotification(final int id) {
        notificationManager.cancel(id);
    }

}

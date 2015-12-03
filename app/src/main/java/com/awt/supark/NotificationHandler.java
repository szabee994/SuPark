package com.awt.supark;

/**
 * Created by Szabolcs on 2015.11.19..
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class NotificationHandler {
    Context context;
    Handler notificationHandler;
    Intent intent;
    PendingIntent pIntent;
    NotificationCompat.Builder mNotification;

    Intent cancelButton;
    PendingIntent btPendingIntent;

    public NotificationHandler(Context cont) {
        context = cont;
    }

    public void throwHandler(Handler hndlr){ // Handler init for communication with MainActivity
        notificationHandler = hndlr;
    }

    public void createNotification(String carName, String licenseNum, final int parkingLength, int parkingZone) {
        // Calculating parking time end
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, parkingLength);

        // Formatting the result
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm (MMM dd.)");
        final String endTime = sdf.format(calendar.getTime());

        final int id = (int) System.currentTimeMillis();

        // Intent of main activity
        intent = new Intent(context, MainActivity.class);
        pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Building the notification
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotification = new NotificationCompat.Builder(context);
                mNotification.setContentTitle("Parking status of " + licenseNum)
                .setSmallIcon(R.mipmap.ic_directions_car_white_24dp);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                            new CountDownTimer(parkingLength * 60000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                    // Log.i("TICK!", "Time remaining: " + millisUntilFinished / 60000 + " sec"); pls no

                                    mNotification.setContentText(millisUntilFinished / 60000 + " minutes remained, ticket due: " + endTime);
                                    mNotification.setProgress(parkingLength, (int) millisUntilFinished / 60000, false);
                                    notificationManager.notify(id, mNotification.build());
                                }

                                public void onFinish() {
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

}

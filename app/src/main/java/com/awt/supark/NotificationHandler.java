package com.awt.supark;

/**
 * Created by Szabolcs on 2015.11.19..
 */

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.os.Handler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class NotificationHandler {
    Context context;
    Handler notificationHandler;
    Intent intent;
    PendingIntent pIntent;
    Notification mNotification;

    Intent cancelButton;
    PendingIntent btPendingIntent;

    public NotificationHandler(Context cont) {
        context = cont;
    }

    public void throwHandler(Handler hndlr){ // Handler init for communication with MainActivity
        notificationHandler = hndlr;
    }

    public void createNotification(String carName, String licenseNum, int parkingLength, int parkingZone) {
        // Calculating parking time end
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, parkingLength);

        // Formatting the result
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm (MMM dd.)");
        String endTime = sdf.format(calendar.getTime());

        int id = (int) System.currentTimeMillis();

        // Intent of main activity
        intent = new Intent(context, MainActivity.class);
        pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Building the notification
        mNotification = new Notification.Builder(context)
                .setContentTitle("Parking status of " + licenseNum)
                .setContentText("Zone: " + parkingZone + ", parking end: " + endTime)
                .setSmallIcon(R.mipmap.ic_directions_car_white_24dp)
                .addAction(0, "Cancel", btPendingIntent)
                .build();

        // Making it ongoing
        //mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        notificationManager.notify(id, mNotification);
    }

}

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

    public void createNotification(String carName, String licenseNum, int remainingTime, int parkingZone) {
        int id = (int) System.currentTimeMillis();

        // Intent of main activity
        intent = new Intent(context, MainActivity.class);
        pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        mNotification = new Notification.Builder(context)
                .setContentTitle(carName + " (" + licenseNum + ") status")
                .setContentText("Parked in Zone " + parkingZone + ", remaining time: " + remainingTime)
                .setSmallIcon(R.mipmap.ic_directions_car_white_48dp)
                .addAction(0, "Cancel", btPendingIntent)
                .build();

        //mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        notificationManager.notify(id, mNotification);
    }

}

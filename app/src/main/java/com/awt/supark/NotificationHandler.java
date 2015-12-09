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
import android.util.Log;

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



}

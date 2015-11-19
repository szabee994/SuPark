package com.awt.supark;

/**
 * Created by Szabolcs on 2015.11.19..
 */

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.os.Handler;

public class NotificationHandler {
    Context context;
    Handler notificationHandler;

    public NotificationHandler(Context cont) {
        context = cont;
    }

    public void throwHandler(Handler hndlr){ // Handler init for communication with MainActivity
        notificationHandler = hndlr;
    }

    public void createNotification(String carName, String licenseNum, int parkingTime, int parkingZone) {




    }

}

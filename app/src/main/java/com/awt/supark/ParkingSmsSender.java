package com.awt.supark;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Szabolcs on 2015.11.09..
 */
public class ParkingSmsSender {
    Context context;
    Handler smsHandler;

    public ParkingSmsSender(Context cont) {
        // Setting the context
        context = cont;
    }

    public void throwHandler(Handler hndlr){ // Handler init for communication with MainActivity
        smsHandler = hndlr;
    }

    public void sendSms(String phoneNumber, int zone, String license) {
        Log.i("smsHandler", "Sending SMS.. Number: " + phoneNumber + ", Zone: " + zone);

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        // Actual SMS message
        String message = license + " - SuPark Test SMS";

        // Intents for SMS state
        PendingIntent sentPI =      PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);

        BroadcastReceiver broadSent = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                // Handling the sms states
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i("smsHandler", "SMS sent");
                        smsHandler.obtainMessage(0).sendToTarget();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.i("smsHandler", "ERR: Generic failure");

                        smsHandler.obtainMessage(2).sendToTarget();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.i("smsHandler", "ERR: No service");
                        smsHandler.obtainMessage(2).sendToTarget();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.i("smsHandler", "ERR: Null PDU");
                        smsHandler.obtainMessage(2).sendToTarget();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.i("smsHandler", "ERR: Radio off");
                        smsHandler.obtainMessage(3).sendToTarget();
                        break;
                }
                // We MUST unregister the receiver
                context.unregisterReceiver(this);
            }
        };

        BroadcastReceiver broadReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                // Handling the sms states
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i("smsHandler", "SMS received");
                        smsHandler.obtainMessage(1).sendToTarget();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("smsHandler", "SMS not received");
                        smsHandler.obtainMessage(2).sendToTarget();
                        break;
                }
                // We MUST unregister the receiver
                context.unregisterReceiver(this);
            }
        };

        // When the SMS has been sent
        context.registerReceiver(broadSent, new IntentFilter(SENT));

        // When the SMS has been delivered
        context.registerReceiver(broadReceive, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

        //smsHandler.obtainMessage(0).sendToTarget();
        //smsHandler.obtainMessage(1).sendToTarget();
    }
}

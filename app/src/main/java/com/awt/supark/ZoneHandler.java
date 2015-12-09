package com.awt.supark;

import android.content.Context;
import android.util.Log;
import android.view.View;

/**
 * Created by doctor on 24/11/2015.
 */
public class ZoneHandler {
    Context context;
    public ZoneHandler(Context cont){
        context = cont;
    }

    // Selects the right sms number according to the current zone
    public String zoneSmsNumSelector(final MainActivity act) {
        return act.zoneSmsNumDb[act.currentZone - 1];
    }

    // Every time the user press a zone changer button this will be called
    public void zoneChangeButtonPressed(View view, final MainActivity act) {
        switch(view.getId()) { // Gets the pressed buttons ID
            case R.id.buttonZone1:
                act.currentZone = 1;
                act.layoutHandler.colorSwitch(1,act);
                act.layoutHandler.activeZoneButton(1, act);
                break;
            case R.id.buttonZone2:
                act.currentZone = 2;
                act.layoutHandler.colorSwitch(2,act);
                act.layoutHandler.activeZoneButton(2, act);
                break;
            case R.id.buttonZone3:
                act.currentZone = 3;
                act.layoutHandler.colorSwitch(3,act);
                act.layoutHandler.activeZoneButton(3, act);
                break;
            case R.id.buttonZone4:
                act.currentZone = 4;
                act.layoutHandler.colorSwitch(4,act);
                act.layoutHandler.activeZoneButton(4, act);
                break;
        }

        act.layoutHandler.updateLocationTextButton(act);    // Updating the text on the layout
        act.locationLocked = true;                          // We must lock the location changes at this point
        act.currentRegion = -1;                             // No region info

        act.imageLocation.clearAnimation();                 // Stopping the blinking location icon...

        Log.i("ZoneChangeButton", "Current zone: " + act.currentZone);
    }
}

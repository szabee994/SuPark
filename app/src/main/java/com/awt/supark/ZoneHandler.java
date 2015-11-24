package com.awt.supark;

import android.content.Context;
import android.util.Log;
import android.view.View;

/**
 * Created by docto on 24/11/2015.
 */
public class ZoneHandler {
    Context context;
    public ZoneHandler(Context cont){
        context = cont;
    }

    // Selects the right sms number according to the current zone
    public String zoneSmsNumSelector(final MainActivity act) {
        String num = "0";
        num = act.zoneSmsNumDb[act.currentZone-1];
        return num;
    }

    // Every time the user press a zone changer button this will be called
    public void zoneChangeButtonPressed(View view, final MainActivity act) {
        // Gets the pressed buttons ID
        switch(view.getId()) {
            case R.id.buttonZone1:
                act.currentZone = 1;
                act.layoutHandler.colorSwitch(1,act);
                break;
            case R.id.buttonZone2:
                act.currentZone = 2;
                act.layoutHandler.colorSwitch(2,act);
                break;
            case R.id.buttonZone3:
                act.currentZone = 3;
                act.layoutHandler.colorSwitch(3,act);
                break;
            case R.id.buttonZone4:
                act.currentZone = 4;
                act.layoutHandler.colorSwitch(4,act);
                break;
        }
        act.layoutHandler.updateLocationTextButton(act);

        act.locationLocked = true;  // If one of the zone changer buttons has been pressed we must lock the zone
        act.currentRegion = -1;

        act.imageLocation.clearAnimation();  // Stopping the blinking location icon...
        //imageLocation.setVisibility(View.INVISIBLE);  // ...and making it invisible

        Log.i("ZoneChangeButton", "Current zone: " + act.currentZone);
    }
}

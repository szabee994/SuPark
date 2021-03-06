package com.awt.supark;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public carHandler CarHandler;

    // Layout variables
    boolean     dimActive = false;          // Dim layers status
    boolean     pullUp = false;             // Is there any layout pulled up
    boolean     pullUpStarted = false;      // Is there any layout BEING pulled up - prevents opening two layouts at the same time
    boolean     animInProgress = false;     // Is there any animation in progress
    boolean     autoLoc = true;
    boolean     lastLicense = true;
    boolean     showTicket = true;
    boolean     debugNumbers = true;
    boolean debugsendsms = true;
    int         openedLayout = 0;           // ID of the current opened

    // Animation timing (ms)
    int         layoutFadeOutDuration = 100;
    int         layoutFadeInDuration = 100;
    int         layoutPullUpDuration = 250;
    int         smallButtonHighlightChangeDuration = 100;

    // Location
    boolean     locationFound = false;      // True if the location has found by GPS signal
    boolean     locationLocked = false;     // True if the location must not change anymore
    int         currentZone = 0;            // User's current zone
    int         currentRegion = -1;         // Current region
    boolean     backDisabled = false;       // True if the back keys functionality needs to be disabled

    // String database
    String currentLicense = "";

    // Context
    Context     cont;

    // Animation variables
    Animation  anim_fade_in;
    Animation  anim_fade_out;
    Animation  anim_zoom_in;
    Animation  anim_zoom_out;
    Animation  anim_slide_up_fade_in;
    Animation  anim_slide_down_fade_out;
    Animation  anim_center_open_up;
    Animation  anim_anticipate_rotate_zoom_out;
    Animation  anim_anticipate_rotate_zoom_in;
    Animation  anim_blink;
    Animation  anim_car_enter;
    Animation  anim_car_leave;

    // UI elements
    ImageButton             btnPark;
    ImageButton             btnZone1;
    ImageButton             btnZone2;
    ImageButton             btnZone3;
    ImageButton             btnZone4;
    TextView                locationInfo;
    ImageView               imageLocation;
    ImageView               imageCar;
    TextView                textParkingScreen;
    FloatingActionButton    btnMap;
    FloatingActionButton    btnCars;
    FloatingActionButton    btnStatistics;
    FloatingActionButton    btnEtc;
    TextView                zonePrice;
    LinearLayout            licensePlate;
    TextView                licenseCity;
    TextView                licenseNum;
    TextView tapHereText1;
    LinearLayout            tapHereText;

    // Layouts
    RelativeLayout      backDimmer;
    FrameLayout         otherContent;
    LinearLayout        contentLinear;
    TableRow            tableRowTopHalf;
    LinearLayout        parkingBackground;
    RelativeLayout      wrapper;

    // Fragment manager
    FragmentManager fragmentManager;
    Fragment        fragment;

    // Parking Data handler
    ParkingDataHandler  parkHandler;
    ParkingSmsSender    smsHandler;
    LayoutHandler       layoutHandler;
    // Zone finder handler
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: // If a parking zone found
                    if (!locationLocked) {
                        locationFound = true;
                        changeRegion(msg.arg2);
                        changeZone(msg.arg1);
                    }
                    break;
                case 1: // When the user moves
                    locationFound = true;
                    break;
                case 3: // When parking data has been sent to server
                    if (msg.arg1 == 1) {
                        //Toast.makeText(cont, getResources().getString(R.string.parking_data_uploaded), Toast.LENGTH_SHORT).show();
                    } else if (msg.arg1 == 2) {
                        Toast.makeText(cont, getResources().getString(R.string.parking_data_fail), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };
    // SMS sender handler
    private final Handler smsResponse = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // SMS sent
                case 0:
                    //Toast.makeText(getApplicationContext(), getResources().getString(R.string.sms_sent), Toast.LENGTH_SHORT).show();
                    break;
                // SMS delivered
                case 1:
                    //Toast.makeText(getApplicationContext(), getResources().getString(R.string.sms_delivered), Toast.LENGTH_SHORT).show();
                    parkingInit("finish");
                    break;
                // Error generic
                case 2:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.sms_error_generic), Toast.LENGTH_LONG).show();
                    parkingInit("error");
                    break;
                // Error radio off
                case 3:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.sms_error_radio), Toast.LENGTH_LONG).show();
                    parkingInit("error");
                    break;
            }
        }
    };
    String[] zoneSmsNumDb = new String[4];
    // Intents
    Intent mServiceIntent;

    // SharedPreferences
    SharedPreferences sharedprefs;
    ZoneHandler zoneHandler;

    MainActivity act = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cont = this;

        // Animations
        anim_zoom_in =                      AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        anim_zoom_out =                     AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        anim_fade_in =                      AnimationUtils.loadAnimation(this, R.anim.fade_in);
        anim_fade_out =                     AnimationUtils.loadAnimation(this, R.anim.fade_out);
        anim_slide_up_fade_in =             AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);
        anim_slide_down_fade_out =          AnimationUtils.loadAnimation(this, R.anim.slide_down_fade_out);
        anim_center_open_up =               AnimationUtils.loadAnimation(this, R.anim.center_open_up);
        anim_anticipate_rotate_zoom_out =   AnimationUtils.loadAnimation(this, R.anim.anticipate_rotate_zoom_out);
        anim_anticipate_rotate_zoom_in =    AnimationUtils.loadAnimation(this, R.anim.anticipate_rotate_zoom_in);
        anim_blink =                        AnimationUtils.loadAnimation(this, R.anim.blink);
        anim_car_enter =                    AnimationUtils.loadAnimation(this, R.anim.car_enter);
        anim_car_leave =                    AnimationUtils.loadAnimation(this, R.anim.car_leave);

        // UI elements
        btnPark =           (ImageButton)           findViewById(R.id.buttonPark);
        btnZone1 =          (ImageButton)           findViewById(R.id.buttonZone1);
        btnZone2 =          (ImageButton)           findViewById(R.id.buttonZone2);
        btnZone3 =          (ImageButton)           findViewById(R.id.buttonZone3);
        btnZone4 =          (ImageButton)           findViewById(R.id.buttonZone4);
        locationInfo =      (TextView)              findViewById(R.id.textViewLocationInfo);
        imageLocation =     (ImageView)             findViewById(R.id.imageLocation);
        imageCar =          (ImageView)             findViewById(R.id.imageCar);
        textParkingScreen = (TextView)              findViewById(R.id.textParkingScreen);
        btnCars =           (FloatingActionButton)  findViewById(R.id.buttonCars);
        btnStatistics =     (FloatingActionButton)  findViewById(R.id.buttonStatistics);
        btnEtc =            (FloatingActionButton)  findViewById(R.id.buttonEtc);
        btnMap =            (FloatingActionButton)  findViewById(R.id.buttonMap);
        zonePrice =         (TextView)              findViewById(R.id.textViewZonePrice);
        licensePlate =      (LinearLayout)          findViewById(R.id.licensePlate);
        licenseCity =       (TextView)              findViewById(R.id.city);
        licenseNum =        (TextView)              findViewById(R.id.num);
        tapHereText =       (LinearLayout)          findViewById(R.id.tapHereText);
        tapHereText1 = (TextView) findViewById(R.id.tapHereText1);

        // Layouts
        backDimmer =        (RelativeLayout)        findViewById(R.id.back_dimmer);
        contentLinear =     (LinearLayout)          findViewById(R.id.contentLinear);
        tableRowTopHalf =   (TableRow)              findViewById(R.id.tableRowTopHalf);
        otherContent =      (FrameLayout)           findViewById(R.id.otherContent);
        parkingBackground = (LinearLayout)          findViewById(R.id.parkingBackground);
        wrapper =           (RelativeLayout)        findViewById(R.id.wrapper);

        sharedprefs =       PreferenceManager.getDefaultSharedPreferences(cont);
        autoLoc =           sharedprefs.getBoolean("autoloc", true);
        lastLicense =       sharedprefs.getBoolean("lastlicenseremember", true);
        showTicket =        sharedprefs.getBoolean("showTicket", true);
        debugNumbers = sharedprefs.getBoolean("debugNumbers", true);
        debugsendsms = sharedprefs.getBoolean("debugsendsms", true);
        setLanguage(sharedprefs.getString("lang", "auto"));
        tapHereText1.setText(getString(R.string.tap_here2));
        if (lastLicense)
            currentLicense = sharedprefs.getString("lastlicense", "");

        fragmentManager =   getSupportFragmentManager();

        // Setting up the handlers
        parkHandler = new ParkingDataHandler(this);
        parkHandler.checkForUpdate();           // Checks that the local database is up to date
        parkHandler.throwHandler(mHandler);     // Initializes the message handler

        smsHandler = new ParkingSmsSender(this);
        smsHandler.throwHandler(smsResponse);   // Initializes the message handler

        layoutHandler = new LayoutHandler(this);
        CarHandler =    new carHandler(this);
        zoneHandler =   new ZoneHandler(this);

        btnPark.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    view.startAnimation(anim_zoom_out);
                    anim_zoom_out.setFillAfter(true);
                } else if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
                    view.startAnimation(anim_zoom_in);
                    anim_zoom_in.setFillAfter(true);
                }

                return false;
            }
        });

        // Setting up a listener to track the click events for the parking button
        btnPark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pullUpStarted) {
                    pullUpStarted = true;
                    parkingInit("start");
                }
            }
        });

        // Turning off zone button highlight
        layoutHandler.activeZoneButton(0, act);

        // If the auto locate setting was enabled starts locating
        if (autoLoc) {
            getGPSzone(null);
        } else {
            currentZone = 0;
            layoutHandler.updateLocationTextButton(act);
        }

        CarHandler.updateLicense(act);
        setDebugNumbers();

        // Starting the background service
        mServiceIntent = new Intent(act, ParkingTimerService.class);
        Log.i("Main", "OnCreate");
        startTimerService(0);
    }

    public void startTimerService(int id) {
        mServiceIntent.putExtra("startId", id);
        startService(mServiceIntent);
    }

    public void parkingInit(String state) {
        parkHandler.parkingInit(state, this);
    }

    public void setLicense(View v, String license) {
        Toast.makeText(cont, getResources().getString(R.string.car_selected), Toast.LENGTH_SHORT).show();
        currentLicense = license;
        CarHandler.updateLicense(act);
        sharedprefs.edit().putString("lastlicense", license).commit();
        smallButtonPressed(findViewById(R.id.buttonCars));
    }

    public void park(String action) {
        parkHandler.park(action, this);
    }

    public void parkingBackgroundShow() {
        layoutHandler.parkingBackgroundShow(this);
    }

    public void setDebugNumbers() {
        if (debugNumbers) {
            zoneSmsNumDb[0] = "+381629775063";
            zoneSmsNumDb[1] = "+381631821336";
            zoneSmsNumDb[2] = "+381621821186";
            zoneSmsNumDb[3] = "+38166424280";
        } else {
            zoneSmsNumDb[0] = "9241";
            zoneSmsNumDb[1] = "9242";
            zoneSmsNumDb[2] = "9243";
            zoneSmsNumDb[3] = "9244";
        }
    }

    // Includes views from XML depending on which button was pressed
    public void otherContentHandler(View view) {
        layoutHandler.otherContentHandler(view, this);
    }

    public void licensePressed(View view) {
        layoutHandler.smallButtonPressed(findViewById(R.id.buttonCars), act);
    }

    public void smallButtonPressed(final View view) {
        layoutHandler.smallButtonPressed(view, this);
    }

    // Same as smallButtonPressed just backwards
    public void pullDown() {
        layoutHandler.pullDown(this);
    }

    // Every time the user press a zone changer button this will be called
    public void zoneChangeButtonPressed(View view) {
        zoneHandler.zoneChangeButtonPressed(view, this);
    }

    public void getGPSzone(View v){
        imageLocation.startAnimation(anim_blink);

        parkHandler.getZone();
        layoutHandler.updateLocationTextGps(act);

        locationLocked = false;
    }

    public void changeRegion(int region) {
        if (!locationLocked) {
            currentRegion = region;
            layoutHandler.updateLocationTextGps(this); // Updates the region text
        }
    }

    // Zone updater
    public void changeZone(int zone) {
        if (!locationLocked) {                              // If the location is not locked, and the user is in a poly we have to
            currentZone = zone;                             // set the zone,
            layoutHandler.colorSwitch(currentZone, this);   // change the background color,
            layoutHandler.activeZoneButton(zone, this);     // highlight the corresponding zone button
            layoutHandler.updateLocationTextGps(this);      // update the status text

            Log.i("SuPark", "Current zone from GPS: " + currentZone + " Region ID: " + currentRegion);
        }
    }

    // Android back key function
    @Override
    public void onBackPressed() {
        if(!backDisabled) {
            if (dimActive) {
                parkingInit("cancel");
            } else if ((pullUp || pullUpStarted) && !animInProgress) {
                // If there is an activity pulled up, pulls down
                pullDown();
            } else {
                finish();
            }
        }
    }

    // New car
    public void openAddCarFragment(final View view) {
        Bundle args = new Bundle();
        args.putInt("editid", -1);
        fragment = new EditCar();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_slideup, R.anim.fragment_fadeout);
        fragmentTransaction.replace(R.id.otherContent, fragment);
        fragmentTransaction.commit();

    }

    // Edit car
    public void openCarFragment(View view, int editid) {
        Bundle args = new Bundle();
        args.putInt("editid", editid);
        fragment = new EditCar();

        if (editid == -1) { fragment = new CarsFragment(); }

        fragment.setArguments(args);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (editid == -1) {
            fragmentTransaction.setCustomAnimations(R.anim.fragment_fadein, R.anim.fragment_slidedown);
        } else {
            fragmentTransaction.setCustomAnimations(R.anim.fragment_slideup, R.anim.fragment_fadeout);
        }

        fragmentTransaction.replace(R.id.otherContent, fragment);
        fragmentTransaction.commit();
    }

    // Back
    public void openCarFragment(View view, boolean animation) {
        fragment = new CarsFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(animation) { fragmentTransaction.setCustomAnimations(R.anim.fragment_fadein, R.anim.fragment_slidedown); }

        fragmentTransaction.replace(R.id.otherContent, fragment);
        fragmentTransaction.commit();
    }

    public void showParkedCar(final View view) {
        if (openedLayout == R.id.buttonMap) {
            MapFragment fragment = (MapFragment) fragmentManager.findFragmentById(R.id.otherContent);
            fragment.showCar();
        }
    }

    public void setLanguage(String lang) {
        sharedprefs.edit().putString("lang", lang).commit();
        if (!lang.equals("auto")) {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    public void autoLocListener(View v) {
        EtcFragment fragment = (EtcFragment) fragmentManager.findFragmentById(R.id.otherContent);
        sharedprefs.edit().putBoolean("autoloc", fragment.autoLoc.isChecked()).apply();
        autoLoc = fragment.autoLoc.isChecked();
    }

    public void toggleDebugNumbers(View v) {
        EtcFragment fragment = (EtcFragment) fragmentManager.findFragmentById(R.id.otherContent);
        sharedprefs.edit().putBoolean("debugNumbers", fragment.debugNumbers.isChecked()).apply();
        debugNumbers = fragment.debugNumbers.isChecked();
        setDebugNumbers();
    }

    public void toggleDebugSendSMS(View v) {
        EtcFragment fragment = (EtcFragment) fragmentManager.findFragmentById(R.id.otherContent);
        sharedprefs.edit().putBoolean("debugsendsms", fragment.debugSendSMS.isChecked()).apply();
        debugsendsms = fragment.debugSendSMS.isChecked();
        setDebugNumbers();
    }

    public void lastLicenseListener(View v) {
        EtcFragment fragment = (EtcFragment) fragmentManager.findFragmentById(R.id.otherContent);
        sharedprefs.edit().putBoolean("lastlicenseremember", fragment.lastLicense.isChecked()).apply();
        lastLicense = fragment.lastLicense.isChecked();
    }

    public void showTicketListener(View v) {
        EtcFragment fragment = (EtcFragment) fragmentManager.findFragmentById(R.id.otherContent);
        sharedprefs.edit().putBoolean("showTicket", fragment.showTicketCheck.isChecked()).apply();
        showTicket = fragment.showTicketCheck.isChecked();
    }

    public void alertBeforeListener(View v) {
        EtcFragment fragment = (EtcFragment) fragmentManager.findFragmentById(R.id.otherContent);
        sharedprefs.edit().putBoolean("alertBefore", fragment.alertBefore.isChecked()).apply();
        showTicket = fragment.alertBefore.isChecked();
    }

    public void alertAfterListener(View v) {
        EtcFragment fragment = (EtcFragment) fragmentManager.findFragmentById(R.id.otherContent);
        sharedprefs.edit().putBoolean("alertAfter", fragment.alertAfter.isChecked()).apply();
        showTicket = fragment.alertAfter.isChecked();
    }

    @Override
    protected void onPause() {
        if (parkHandler.locationManager != null) {
            try {
                parkHandler.locationManager.removeUpdates(parkHandler);
            } catch (SecurityException e) {
                Log.i("SecExp", e.toString());
            }
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (autoLoc) {
            parkHandler.getZone();
        }
        super.onResume();
    }
}
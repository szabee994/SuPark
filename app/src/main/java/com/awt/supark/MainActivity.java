package com.awt.supark;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.awt.supark.Model.Car;

public class MainActivity extends AppCompatActivity {
    // Layout
    boolean dimActive = false;  // Dim layers status
    boolean pullUp = false;  // Is there any layout pulled up
    boolean pullUpStarted = false;  // Is there any layout BEING pulled up - prevents opening two layouts at the same time
    boolean animInProgress = false;  // Is there any animation in progress
    int openedLayout = 0;  // ID of the current opened otherContent

    // Animation times in ms
    int layoutFadeOutDuration = 150;
    int layoutFadeInDuration = 150;
    int layoutPullUpDuration = 300;
    int smallButtonHighlightChangeDuration = 150;

    // Location
    boolean locationFound = false;  // True if the location has found by GPS signal
    boolean locationLocked = false;  // True if the location must not change anymore
    int currentZone = 0;  // User's current zone
    int currentRegion = -1;  // Current region

    boolean backDisabled = false;  // True if the back keys functionality needs to be disabled

    // String database
    String[] licenseNumberDb = {""};

    /*                         sebők             dani              andi             mark
       Zone SMS numbers         ZONE1            ZONE2            ZONE3            ZONE4  */
    String[] zoneSmsNumDb = {"+381629775063", "+381631821336", "+381621821186", "+38166424280"};

    // Context
    Context cont;

    // Animation variables
    Animation anim_fade_in;
    Animation anim_fade_out;
    Animation anim_zoom_in;
    Animation anim_zoom_out;
    Animation anim_slide_up_fade_in;
    Animation anim_slide_down_fade_out;
    Animation anim_center_open_up;
    Animation anim_anticipate_rotate_zoom_out;
    Animation anim_anticipate_rotate_zoom_in;
    Animation anim_blink;
    Animation anim_car_enter;
    Animation anim_car_leave;

    // UI elements
    ImageButton btnPark;
    AutoCompleteTextView licenseNumber;
    ImageButton btnZone1;
    ImageButton btnZone2;
    ImageButton btnZone3;
    ImageButton btnZone4;
    TextView locationInfo;
    ImageView imageLocation;
    ImageView imageCar;
    TextView textParkingScreen;
    FloatingActionButton btnMap;
    FloatingActionButton btnCars;
    FloatingActionButton btnStatistics;
    FloatingActionButton btnEtc;

    // Layouts
    RelativeLayout backDimmer;
    FrameLayout otherContent;
    LinearLayout contentLinear;
    TableRow tableRowTopHalf;
    LinearLayout parkingBackground;
    RelativeLayout wrapper;

    // License number database adapter
    ArrayAdapter<String> licenseNumberDbAdapter;

    // Fragment manager
    FragmentManager fragmentManager;

    // Parking Data handler
    ParkingDataHandler parkHandler;
    ParkingSmsSender smsHandler;

    // Edit Boolean
    //boolean edit = false;

    // ----------------------------------- THREAD MESSAGE HANDLER ---------------------------------------------
    // Zone finder handler
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    // If the device is in a polygon receives the region and zone info
                    if(!locationLocked) {
                        currentRegion = msg.arg2;
                        changeZone(msg.arg1);
                        locationFound = true;
                        updateLocationTextGps();
                    }
                    break;
                case 1:
                    /* This occurs every time the user moves. Because the locationFound
                     * changes to true but the currentZone remains in it's initial
                     * state (0), the program will know that the user is not in any
                     * parking zone.
                     */
                    locationFound = true;
                    break;
                case 3:
                    // Occurs when parking data has been sent to server
                    if (msg.arg1 == 1) {
                        Toast.makeText(cont, getResources().getString(R.string.parking_data_uploaded), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.sms_sent), Toast.LENGTH_SHORT).show();
                    break;
                // SMS delivered
                case 1:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.sms_delivered), Toast.LENGTH_SHORT).show();
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

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cont = this;

        // Animations
        anim_zoom_in = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        anim_zoom_out = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        anim_fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        anim_fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        anim_slide_up_fade_in = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);
        anim_slide_down_fade_out = AnimationUtils.loadAnimation(this, R.anim.slide_down_fade_out);
        anim_center_open_up = AnimationUtils.loadAnimation(this, R.anim.center_open_up);
        anim_anticipate_rotate_zoom_out = AnimationUtils.loadAnimation(this, R.anim.anticipate_rotate_zoom_out);
        anim_anticipate_rotate_zoom_in = AnimationUtils.loadAnimation(this, R.anim.anticipate_rotate_zoom_in);
        anim_blink = AnimationUtils.loadAnimation(this, R.anim.blink);
        anim_car_enter = AnimationUtils.loadAnimation(this, R.anim.car_enter);
        anim_car_leave = AnimationUtils.loadAnimation(this, R.anim.car_leave);

        // UI elements
        btnPark = (ImageButton) findViewById(R.id.buttonPark);
        btnZone1 = (ImageButton) findViewById(R.id.buttonZone1);
        btnZone2 = (ImageButton) findViewById(R.id.buttonZone2);
        btnZone3 = (ImageButton) findViewById(R.id.buttonZone3);
        btnZone4 = (ImageButton) findViewById(R.id.buttonZone4);
        locationInfo = (TextView) findViewById(R.id.textViewLocationInfo);
        imageLocation = (ImageView) findViewById(R.id.imageLocation);
        imageCar = (ImageView) findViewById(R.id.imageCar);
        textParkingScreen = (TextView) findViewById(R.id.textParkingScreen);
        btnCars = (FloatingActionButton) findViewById(R.id.buttonCars);
        btnStatistics = (FloatingActionButton) findViewById(R.id.buttonStatistics);
        btnEtc = (FloatingActionButton) findViewById(R.id.buttonEtc);
        btnMap = (FloatingActionButton) findViewById(R.id.buttonMap);

        btnZone1.setAlpha(0.3f);
        btnZone2.setAlpha(0.3f);
        btnZone3.setAlpha(0.3f);
        btnZone4.setAlpha(0.3f);

        // Layouts
        backDimmer = (RelativeLayout) findViewById(R.id.back_dimmer);
        contentLinear = (LinearLayout) findViewById(R.id.contentLinear);
        tableRowTopHalf = (TableRow) findViewById(R.id.tableRowTopHalf);
        otherContent = (FrameLayout) findViewById(R.id.otherContent);
        parkingBackground = (LinearLayout) findViewById(R.id.parkingBackground);
        wrapper = (RelativeLayout) findViewById(R.id.wrapper);

        setLicenseToArray();
        updateLocationTextGps();
        fragmentManager = getSupportFragmentManager();

        // Setting up the handlers
        parkHandler = new ParkingDataHandler(this);
        parkHandler.checkForUpdate();  // Checks that the local database is up to date
        parkHandler.throwHandler(mHandler);  // Initializes the message handler
        parkHandler.getZone();  // Gets zone info

        smsHandler = new ParkingSmsSender(this);
        smsHandler.throwHandler(smsResponse);  // Initializes the message handler

        // -----------------------------------------------------------------------------------------------------------------

        // Setting up a listener to track the touch/release events for the parking button
        btnPark.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                /* If the user taps the button zooming in animation starts.
                 * The animation will remain in it's final state (pressed).
                 */
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
                if(!pullUpStarted) {
                    pullUpStarted = true;
                    parkingInit("start");
                }
            }
        });
    }

    /* -------------------------------------- PARKING MANAGER FUNCTION STARTS HERE ------------------------------------------
     *
     * First we have to call the initialization function which will clean up thIe UI so we can show up other layouts later.
     * States:
     * 1. start
     *   * Initializes parking sequence by checking the zone number
     *   * Starts the parking animations
     *   * Calls the parking function
     * 2. cancel
     *   * Cancels the parking
     *   * Rolls back every changes
     * 3. finish
     *   * Tells the user that the parking process was successfull
     *   * Removes every parking layer
     * 4. error
     */

    public void parkingInit(String state) {
        if (state.equals("start")) {
            if(currentZone == 0) {
                Toast.makeText(cont, getResources().getString(R.string.wait_for_zone), Toast.LENGTH_LONG).show();
                pullUpStarted = false;
            } else {
                parkingBackground.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                textParkingScreen.setText(getResources().getString(R.string.please_wait));

                btnPark.startAnimation(anim_anticipate_rotate_zoom_out);
                anim_anticipate_rotate_zoom_out.setFillAfter(true);

                pullUpStarted = false;

                backDimmer.setVisibility(View.VISIBLE);  // Sets the dim layer visible.
                backDimmer.startAnimation(anim_fade_in);  // Starts a fade in animation on the dimming layer
                dimActive = true;  // Sets the dim visibility indicator variable to true

                // Making the layout visible
                parkingBackgroundShow();

                // Car enters
                imageCar.startAnimation(anim_car_enter);
                anim_car_enter.setFillAfter(true);

                anim_car_enter.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        park("send");  // Calls the parking function
                        Log.i("SuPark", "Current zone: " + currentZone);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        } else if(state.equals("cancel")) {
            // Fades out the dimming layer
            backDimmer.startAnimation(anim_fade_out);
            park("cancel");

            // At the end of the animation sets the dimming layers visibility to 'gone'
            anim_fade_out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    backDisabled = false;
                    backDimmer.setVisibility(View.GONE);
                    parkingBackground.setVisibility(View.INVISIBLE);
                    btnPark.startAnimation(anim_anticipate_rotate_zoom_in);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            dimActive = false;
        } else if(state.equals("finish")){
            park("finish");
            appBackgroundColorChange(parkingBackground, 300, 46, 125, 50);
            textParkingScreen.setText(getResources().getString(R.string.success));

            imageCar.startAnimation(anim_car_leave);
            anim_car_leave.setFillAfter(true);

            // At the end of the animation sets the dimming layers visibility to 'gone'
            anim_car_leave.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    backDimmer.startAnimation(anim_fade_out);
                    anim_fade_out.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            backDimmer.setVisibility(View.GONE);
                            dimActive = false;
                            parkingBackground.setVisibility(View.INVISIBLE);
                            btnPark.startAnimation(anim_anticipate_rotate_zoom_in);
                            backDisabled = false;

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else if(state.equals("error")){
            // Fades out the dimming layer
            backDimmer.startAnimation(anim_fade_out);
            park("error");

            // At the end of the animation sets the dimming layers visibility to 'gone'
            anim_fade_out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    backDimmer.setVisibility(View.GONE);
                    dimActive = false;
                    parkingBackground.setVisibility(View.INVISIBLE);
                    btnPark.startAnimation(anim_anticipate_rotate_zoom_in);
                    backDisabled = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    // Selects the right sms number according to the current zone
    public String zoneSmsNumSelector() {
        String num = "0";

        switch (currentZone) {
            case 1:
                num = zoneSmsNumDb[0];
                break;
            case 2:
                num = zoneSmsNumDb[1];
                break;
            case 3:
                num = zoneSmsNumDb[2];
                break;
            case 4:
                num = zoneSmsNumDb[3];
                break;
        }
        return num;
    }

    /* This is the actual parking function. You can call it with one of the next parameters:
     * 1. send - With this parameter the parking function will send a parking query
     * 2. cancel - Cancels the parking query
     * 3. error
     */
    public void park(String action) {
        if(action == "send") {
            Log.i("MainActivity", "Parking started");
            backDisabled = true;
            smsHandler.sendSms(zoneSmsNumSelector(), currentZone);
            parkHandler.postPark(currentRegion, currentZone, 60);
        } else if (action == "cancel"){
            Log.i("MainActivity", "Parking cancelled");
            Toast.makeText(cont, getResources().getString(R.string.parking_cancelled), Toast.LENGTH_SHORT).show();
        } else if(action == "finish"){
            Log.i("MainActivity", "Parking finished");
            Toast.makeText(cont, getResources().getString(R.string.parking_success), Toast.LENGTH_LONG).show();
        } else if(action == "error"){
            Log.i("MainActivity", "Parking error");

        }
    }

    public void parkingBackgroundShow() {
        if(dimActive) {  // Only works when the dimming layer is visible
            parkingBackground.setVisibility(View.VISIBLE);  // Turns on the parking background layout
            parkingBackground.startAnimation(anim_center_open_up);  // Animates the parking background layout
        }
    }


    /* ---------------------------------------- LAYOUT PULL-UP/-DOWN FUNCTION STARTS HERE ------------------------------------------
     *
     * Includes views from XML depending on which button was pressed
     */
    public void otherContentHandler(View view) {
        Fragment fragment = null;

        // Gets the pressed buttons ID
        switch (view.getId()) {
            case R.id.buttonMap:
                fragment = new MapFragment();
                break;
            case R.id.buttonStatistics:
                fragment = new StatsFragment();
                break;
            case R.id.buttonCars:
                fragment = new CarsFragment();
                break;
            case R.id.buttonEtc:
                fragment = new EtcFragment();
                break;
        }

        // If there's no fragment
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.otherContent, fragment);
            fragmentTransaction.commit();
            openedLayout = view.getId();
            // Sets the openedLayout variable so we know which one of the foreign layout was opened
        }
    }

    public void smallButtonPressed(final View view) {
        if (!pullUp && !pullUpStarted) { // If it isn't already up
            pullUpStarted = true;
            // Declaring animator
            ValueAnimator animation = ValueAnimator.ofFloat(1f, 0.17f);

            // ****** UI ELEMENTS FADING OUT ANIMATION ******
            // Sets the animation properties
            animation.setDuration(layoutFadeOutDuration);

            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();

                    // Fades out contentLinear and all buttons, except the one that is pressed
                    contentLinear.setAlpha(value);
                    if (view.getId() != R.id.buttonMap) {
                        btnMap.setAlpha(value);
                    }
                    if (view.getId() != R.id.buttonCars) {
                        btnCars.setAlpha(value);
                    }
                    if (view.getId() != R.id.buttonStatistics) {
                        btnStatistics.setAlpha(value);
                    }
                    if (view.getId() != R.id.buttonEtc) {
                        btnEtc.setAlpha(value);
                    }
                }
            });

            animation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // Sets the visibility of the other layout and contentlinear
                    contentLinear.setVisibility(View.GONE);
                    otherContent.setVisibility(View.VISIBLE);

                    // ****** BUTTON PULL UP ANIMATION ******
                    // Declaring animator
                    ValueAnimator nextAnimation = ValueAnimator.ofFloat(1f, 0f);

                    // Sets the animation properties
                    nextAnimation.setDuration(layoutPullUpDuration);
                    nextAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

                    nextAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (float) animation.getAnimatedValue();

                            // Sets weight of the two layouts, this makes one smaller and the other bigger
                            tableRowTopHalf.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, value));
                            otherContent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1 - value));
                        }
                    });

                    // ****** LAYOUT PULL UP ANIMATION ******
                    nextAnimation.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            otherContentHandler(view); // Takes care of including new views
                            otherContent.startAnimation(anim_slide_up_fade_in); // Animates the new activity
                            pullUp = true; // Changing the pull up status indicator
                            pullUpStarted = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            pullUpStarted = false;
                            otherContent.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    nextAnimation.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animation.start();
        } else if (view.getId() == openedLayout){
            pullDown(); // If there is a layout already pulled up we have to pull it down

        } else if (pullUp && (openedLayout != 0) && !pullUpStarted) {
            pullUpStarted = true; // To prevent more than one highlight

            // Changing highlight from previous to current button
            ValueAnimator animation = ValueAnimator.ofFloat(0.17f, 1f);
            animation.setDuration(smallButtonHighlightChangeDuration);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    findViewById(view.getId()).setAlpha(value);
                    findViewById(openedLayout).setAlpha(1.17f - value);
                }
            });
            animation.start();

            // Fades out current layout
            otherContent.startAnimation(anim_fade_out);
            anim_fade_out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    otherContentHandler(view);  // Switches the layout to the new one
                    otherContent.startAnimation(anim_slide_up_fade_in); // Fades in the new layout
                    pullUpStarted=false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

    }

    // Same as smallButtonPressed just backwards
    public void pullDown() {
        animInProgress = true;

        // ****** BUTTON AND LAYOUT PULL DOWN ANIMATION
        // Declaring animator
        ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);

        // Sets the animation properties
        animation.setDuration(layoutPullUpDuration);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                tableRowTopHalf.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, value));
                btnMap.setAlpha(value);
                btnCars.setAlpha(value);
                btnStatistics.setAlpha(value);
                btnEtc.setAlpha(value);
            }
        });

        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Starts layout pull down and fade out animation
                otherContent.startAnimation(anim_slide_down_fade_out);
                anim_slide_down_fade_out.setFillAfter(true);

                // Makes layout invisible
                otherContent.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                contentLinear.setVisibility(View.VISIBLE);

                // ****** UI ELEMENTS FADE IN ANIMATION ******
                // Declaring animator
                ValueAnimator nextAnimation = ValueAnimator.ofFloat(0.17f, 1f);

                // Sets the animation properties
                nextAnimation.setDuration(layoutFadeInDuration);
                nextAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        contentLinear.setAlpha(value);
                    }
                });
                nextAnimation.start();

                pullUp = false;
                openedLayout = 0;
                animInProgress = false;
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.remove(fragmentManager.findFragmentById(R.id.otherContent));
                transaction.commit();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animation.start();
    }

    /* -------------------------------------- ZONE CONTROLLING FUNCTIONS STARTS HERE ---------------------------------------------
     *
     * Every time the user press a zone changer button this will be called
     */
    public void zoneChangeButtonPressed(View view) {
        // Gets the pressed buttons ID
        switch(view.getId()) {
            case R.id.buttonZone1:
                currentZone = 1;
                colorSwitch(1);
                break;
            case R.id.buttonZone2:
                currentZone = 2;
                colorSwitch(2);
                break;
            case R.id.buttonZone3:
                currentZone = 3;
                colorSwitch(3);
                break;
            case R.id.buttonZone4:
                currentZone = 4;
                colorSwitch(4);
                break;
        }
        updateLocationTextButton();

        locationLocked = true;  // If one of the zone changer buttons has been pressed we must lock the zone
        currentRegion = -1;

        imageLocation.clearAnimation();  // Stopping the blinking location icon...
        //imageLocation.setVisibility(View.INVISIBLE);  // ...and making it invisible

        Log.i("ZoneChangeButton", "Current zone: " + currentZone);
    }

    public void getGPSzone(View v){
        imageLocation.startAnimation(anim_blink);
        locationLocked = false;
    }

    // Background color switcher
    public void colorSwitch(int zone) {
        int fadeTime = 600;
        switch(zone) {
            case 1:
                appBackgroundColorChange(wrapper, fadeTime, 183, 28, 28);  // Color of red zone (RGB)
                break;
            case 2:
                appBackgroundColorChange(wrapper, fadeTime, 255, 160, 0);  // Color of yellow zone (RGB)
                break;
            case 3:
                appBackgroundColorChange(wrapper, fadeTime, 0, 121, 107);  // Color of green zone (RGB)
                break;
            case 4:
                appBackgroundColorChange(wrapper, fadeTime, 1, 87, 155);  // Color of blue zone (RGB)
                break;
        }
    }

    // Zone updater
    public void changeZone(int zone) {
        if(currentZone != zone){
            currentZone = zone;
            colorSwitch(currentZone);
            Log.i("SuPark", "Current zone from GPS: " + currentZone + " Region ID: " + currentRegion);
        }
    }

    // Background color changer function
    public void appBackgroundColorChange(View view, int time, int r, int g, int b) {
        ColorDrawable wBack = (ColorDrawable) view.getBackground();
        int color = wBack.getColor();
        int r1 = Color.red(color);
        int g1 = Color.green(color);
        int b1 = Color.blue(color);

        ObjectAnimator colorFade = ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), Color.argb(255, r1, g1, b1), Color.argb(255, r, g, b));
        colorFade.setDuration(time);
        colorFade.start();
    }

    public void updateLocationTextGps() {
        if(locationFound) {
            imageLocation.clearAnimation();  // Stops the blinking GPS image
            switch(currentZone) {
                case 0:
                    locationInfo.setText(getResources().getString(R.string.nozone_auto));
                    break;
                case 1:
                    locationInfo.setText(getResources().getString(R.string.zone1auto));
                    break;
                case 2:
                    locationInfo.setText(getResources().getString(R.string.zone2auto));
                    break;
                case 3:
                    locationInfo.setText(getResources().getString(R.string.zone3auto));
                    break;
                case 4:
                    locationInfo.setText(getResources().getString(R.string.zone4auto));
                    break;
            }
        } else {
            imageLocation.startAnimation(anim_blink);
            locationInfo.setText(getResources().getString(R.string.locating));
        }
    }

    public void updateLocationTextButton() {
        switch(currentZone) {
            case 1:
                locationInfo.setText(getResources().getString(R.string.zone1selected));
                break;
            case 2:
                locationInfo.setText(getResources().getString(R.string.zone2selected));
                break;
            case 3:
                locationInfo.setText(getResources().getString(R.string.zone3selected));
                break;
            case 4:
                locationInfo.setText(getResources().getString(R.string.zone4selected));
                break;
        }
    }
    // -------------------------------------- ZONE CONTROLLING FUNCTIONS ENDS HERE ---------------------------------------------

    // Android back key function
    @Override
    public void onBackPressed() {
        Log.i("MainActivity", "Back pressed");
        if(!backDisabled) {
            /* If the dimming layer is visible then makes invisible, otherwise
             * triggers the default action of back button.
             */
            if (dimActive) {
                // Deactivates the dimming
                parkingInit("cancel");
            } else if ((pullUp || pullUpStarted) && !animInProgress) {
                // If there is an activity pulled up, pulls down
                pullDown();
            } else {
                // Default action
                finish();
            }
        }
    }

    public void openAddCarFragment(final View view){
        Bundle args = new Bundle();
        args.putInt("editid", -1);
        Fragment fragment = new EditCar();
        if (fragment != null) {
            fragment.setArguments(args);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.otherContent, fragment);
            fragmentTransaction.commit();
        }
    }

    public void openCarFragment(final View view, int editid){
        Bundle args = new Bundle();
        args.putInt("editid", editid);
        Fragment fragment = new EditCar();
        if(editid == -1) {
            fragment = new CarsFragment();
        }
        if (fragment != null) {
            fragment.setArguments(args);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.otherContent, fragment);
            fragmentTransaction.commit();
        }
    }

    public void openCarFragment(final View view){
        Fragment fragment;
        fragment = new CarsFragment();
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.otherContent, fragment);
            fragmentTransaction.commit();
        }
    }

    // ----------------------------- Set License to autoCorrect array--------------------------------
    SQLiteDatabase db;
    public void setLicenseToArray(){

        db = SQLiteDatabase.openDatabase(this.getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        db.execSQL("CREATE TABLE IF NOT EXISTS `cars` (\n" +
                "  `car_id` int(2) NOT NULL ,\n" +
                "  `car_name` varchar(100) NOT NULL,\n" +
                "  `car_license` varchar(100) NOT NULL,\n" +
                "  PRIMARY KEY (`car_id`)\n" +
                ")");

        int numberOfCars;

        Cursor d = db.rawQuery("SELECT * FROM cars", null);
        String[] cars = new String[d.getCount()];
        numberOfCars = d.getCount();
        Log.i("Number",Integer.toString(numberOfCars));
        if(numberOfCars > 0) {
            licenseNumberDb = new String[numberOfCars];
            int i = 0;
            for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
                int carlicenseindex = d.getColumnIndex("car_license");
                licenseNumberDb[i] = d.getString(carlicenseindex);
                i++;
            }
        }

        // Loading license numbers database into the UI element licenseNumber (AutoCompleteTextView)
        licenseNumberDbAdapter = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item, licenseNumberDb);
        licenseNumber = (AutoCompleteTextView) findViewById(R.id.licenseNumber);
        licenseNumber.setThreshold(1);  // Starts the matching after one letter entered
        licenseNumber.setAdapter(licenseNumberDbAdapter);  // Applying the adapter

    }
}


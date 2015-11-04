package com.awt.supark;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity { //Needs FragmentActivity
    boolean dimActive = false;  // Holds the dim layers status (true = visible, false = invisible/gone)
    boolean pullUp = false;  // Is there any layout pulled up
    boolean pullUpStarted = false;  // Is there any layout BEING pulled up - prevents opening two layouts at the same time
    boolean animInProgress = false;
    int currentZone = 0;
    int openedLayout = 0; // ID of the current opened otherContent
    // Sample string database stuff
    String[] licenseNumberDb = {"sample1", "sample2", "sample3", "sample4", "sample5"};

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
    Animation anim_zone_fade_in;
    Animation anim_zone_fade_out;

    // UI elements
    ImageButton btnPark;
    ImageButton btnMap;
    ImageButton btnCars;
    ImageButton btnStatistics;
    ImageButton btnEtc;
    AutoCompleteTextView licenseNumber;
    ImageButton btnZone1;
    ImageButton btnZone2;
    ImageButton btnZone3;
    ImageButton btnZone4;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        anim_zone_fade_in = AnimationUtils.loadAnimation(this, R.anim.zone_fade_in);
        anim_zone_fade_out = AnimationUtils.loadAnimation(this, R.anim.zone_fade_out);

        // UI elements
        btnPark = (ImageButton) findViewById(R.id.buttonPark);
        btnCars = (ImageButton) findViewById(R.id.buttonCars);
        btnStatistics = (ImageButton) findViewById(R.id.buttonStatistics);
        btnEtc = (ImageButton) findViewById(R.id.buttonEtc);
        btnMap = (ImageButton) findViewById(R.id.buttonMap);
        btnZone1 = (ImageButton) findViewById(R.id.buttonZone1);
        btnZone2 = (ImageButton) findViewById(R.id.buttonZone2);
        btnZone3 = (ImageButton) findViewById(R.id.buttonZone3);
        btnZone4 = (ImageButton) findViewById(R.id.buttonZone4);

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

        // -----------------------------------------------------------------------------------------------------------------

        // Loading license numbers database into the UI element licenseNumber (AutoCompleteTextView)
        licenseNumberDbAdapter = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item, licenseNumberDb);
        licenseNumber = (AutoCompleteTextView) findViewById(R.id.licenseNumber);
        licenseNumber.setThreshold(1);  // Starts the matching after one letter entered
        licenseNumber.setAdapter(licenseNumberDbAdapter);  // Applying the adapter

        fragmentManager = getSupportFragmentManager();

        // -----------------------------------------------------------------------------------------------------------------

        getZone();

        // Setting up a listener to track the touch/release events for the parking button
        btnPark.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                // If the user taps the button zooming in animation starts.
                // The animation will remain in it's final state (pressed).
                if (motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    // Does the animation
                    view.startAnimation(anim_zoom_out);
                    anim_zoom_out.setFillAfter(true);
                }

                // If the user releases the button zooming out animation starts.
                // The animation will remain in it's final state (released).
                else if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
                    // Do the animation
                    view.startAnimation(anim_zoom_in);
                    anim_zoom_in.setFillAfter(true);
                }

                // This is required for some reasons.
                return false;
            }
        });

        // Setting up a listener to track the click events for the parking button
        btnPark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!pullUpStarted) {
                    pullUpStarted = true;
                    // Initializes the parking
                    parkingInit(true);
                    view.startAnimation(anim_anticipate_rotate_zoom_out);
                    anim_anticipate_rotate_zoom_out.setFillAfter(true);
                    pullUpStarted=false;
                }
            }
        });
    }

    // -------------------------------------- PARKING MANAGER FUNCTION STARTS HERE ------------------------------------------
    //
    // First we have to call the initialization function which will clean up thIe UI so we can show up other layouts later.
    // It has two states: true = dims the screen and calls parking function, false = normal or returns to normal mode
    public void parkingInit(boolean turnOn) {
        if (turnOn) {
            backDimmer.setVisibility(View.VISIBLE);  // Sets the dim layer visible.
            backDimmer.startAnimation(anim_fade_in);  // Starts a fade in animation on the dimming layer

            anim_fade_in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    park("send");  // Calls the parking function
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            dimActive = true;  // Sets the dim visibility indicator variable to true
        }
        else {
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
                    backDimmer.setVisibility(View.GONE);
                    parkingBackground.setVisibility(View.INVISIBLE);
                    btnPark.startAnimation(anim_anticipate_rotate_zoom_in);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            dimActive = false;
        }
    }

    // This is the actual parking function. You can call it with one of the next parameters:
    // 1. send - With this parameter the parking function will send a parking query
    // 2. cancel - Cancels the parking query
    // 3. ...
    // 4. ...
    public void park(String action) {
        if(action == "send") {
            Log.i("MainActivity", "Parking started");
            parkingBackgroundShow();  // Makes the parking process layout visible
        }
        else if (action == "cancel"){
            // TODO:
            // If the user cancels the action
            Log.i("MainActivity", "Parking cancelled");
            Toast.makeText(getApplicationContext(), "Parking cancelled...", Toast.LENGTH_SHORT).show();
        }

    }
    // -------------------------------------- PARKING MANAGER FUNCTION ENDS HERE ---------------------------------------------

    public void parkingBackgroundShow() {
        if(dimActive) {  // Only works when the dimming layer is visible
            parkingBackground.setVisibility(View.VISIBLE);  // Turns on the parking background layout
            parkingBackground.startAnimation(anim_center_open_up);  // Animates the parking background layout
        }
    }

    // Includes views from XML depending on which button was pressed
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

        // If there's no fragment (?)
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.otherContent, fragment);
            fragmentTransaction.commit();
            openedLayout = view.getId();  // Sets the openedLayout variable so we know which one of the foreign layout was opened
        }
    }

    public void smallButtonPressed(final View view) {
        if (!pullUp && !pullUpStarted) { // If it isn't already up
            pullUpStarted = true;
            // Declaring animator
            ValueAnimator animation = ValueAnimator.ofFloat(1f, 0.17f);

            // ****** UI ELEMENTS FADING OUT ANIMATION ******
            // Sets the animation properties
            animation.setDuration(200);

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
                    nextAnimation.setDuration(300);
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
        }
        else if (view.getId() == openedLayout){
            pullDown(); // If there is a layout already pulled up we have to pull it down
        }
        else if (pullUp && (openedLayout != 0) && !pullUpStarted) {
            pullUpStarted=true; //To prevent more than one highlight
            //Log.i("enye", "view:" + view.getId() + ", int:" + openedLayout);

            //Changing highlight from previous to current button
            ValueAnimator animation = ValueAnimator.ofFloat(0.17f, 1f);
            animation.setDuration(150);
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
        animation.setDuration(300);
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
                nextAnimation.setDuration(150);
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

    public void zoneChangeButtonPressed(View view) {
        switch(view.getId()) {
            case R.id.buttonZone1:
                currentZone = 1;
                appBackgroundColorChange(255, 60, 21);  // Color of red zone (RGB)
                break;
            case R.id.buttonZone2:
                currentZone = 2;
                appBackgroundColorChange(252, 254, 94);  // Color of yellow zone (RGB)
                break;
            case R.id.buttonZone3:
                currentZone = 3;
                appBackgroundColorChange(43, 174, 40);  // Color of green zone (RGB)
                break;
            case R.id.buttonZone4:
                currentZone = 4;
                appBackgroundColorChange(63, 95, 255);  // Color of blue zone (RGB)
                break;
        }
        Log.i("SuPark", "Current zone: " + currentZone);
    }

    public void getZone() {
        // TODO:
        // Get parking zone via GPS

        currentZone = 0; // Failed to get zone
        Log.i("SuPark", "Failed to get zone info via GPS.");
        Log.i("SuPark", "Current zone: " + currentZone);
    }

    public void appBackgroundColorChange(int r, int g, int b) {
        ColorDrawable wBack = (ColorDrawable) wrapper.getBackground();
        int color = wBack.getColor();
        int r1 = Color.red(color);
        int g1 = Color.green(color);
        int b1 = Color.blue(color);

        ObjectAnimator colorFade = ObjectAnimator.ofObject(wrapper, "backgroundColor", new ArgbEvaluator(), Color.argb(255, r1, g1, b1), Color.argb(255, r, g, b));
        colorFade.setDuration(1000);
        colorFade.start();
    }

    // Android back key function
    @Override
    public void onBackPressed() {
        Log.i("MainActivity", "Back pressed");

        // If the dimming layer is visible then makes invisible, otherwise
        // triggers the default action of back button.
        if (dimActive) {
            // Deactivates the dimming
            parkingInit(false);
        }
        else if ((pullUp || pullUpStarted) && !animInProgress) {
            // If there is an activity pulled up, pulls down
            pullDown();
        }
        else {
            // Default action
            finish();
        }
    }
}

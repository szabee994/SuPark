package com.awt.supark;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;

public class MainActivity extends AppCompatActivity {
    boolean dimActive = false;  // Holds the dim layers status (true = visible, false = invisible/gone)
    boolean pullUp = false;  // Is there any layout pulled up

    // Animation variables
    Animation anim_fade_in;
    Animation anim_fade_out;
    Animation anim_zoom_in;
    Animation anim_zoom_out;

    // UI elements
    ImageButton btnPark;
    ImageButton btnMap;
    ImageButton btnCars;
    ImageButton btnStatistics;
    ImageButton btnEtc;

    // Layouts
    RelativeLayout backDimmer;
    FrameLayout otherContent;
    LinearLayout contentLinear;
    TableRow tableRowTopHalf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Animations
        anim_zoom_in = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        anim_zoom_out = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        anim_fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        anim_fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        // UI elements
        btnPark = (ImageButton) findViewById(R.id.buttonPark);
        btnCars = (ImageButton) findViewById(R.id.buttonCars);
        btnStatistics = (ImageButton) findViewById(R.id.buttonStatistics);
        btnEtc = (ImageButton) findViewById(R.id.buttonEtc);
        btnMap = (ImageButton) findViewById(R.id.buttonMap);

        // Layouts
        backDimmer = (RelativeLayout) findViewById(R.id.back_dimmer);
        contentLinear = (LinearLayout) findViewById(R.id.contentLinear);
        tableRowTopHalf = (TableRow) findViewById(R.id.tableRowTopHalf);
        otherContent = (FrameLayout) findViewById(R.id.otherContent);

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
                // If the user clicked the parking button we should dim the background
                // by setting the dimming layer visible.
                dimBackground(true);
            }
        });
    }

    // Background dimming function (true = dimmed, false = normal)
    public void dimBackground(boolean turnOn) {
        if (turnOn) {
            // Sets the dim layer visible.
            backDimmer.setVisibility(View.VISIBLE);

            // Starts a fade in animation on the dimming layer
            backDimmer.startAnimation(anim_fade_in);

            // Sets the dim visibility indicator variable to true
            dimActive = true;
        }
        else {
            // Fades out the dimming layer
            backDimmer.startAnimation(anim_fade_out);

            // At the end of the animation sets the dimming layers visibility to 'gone'
            anim_fade_out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    backDimmer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            // Sets the dim visibility indicator variable to false
            dimActive = false;
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
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.otherContent, fragment);
            fragmentTransaction.commit();
        }
    }

    public void smallButtonPressed(final View view) {
        if (!pullUp) { // If it isn't already up
            // Declaring animator
            ValueAnimator animation = ValueAnimator.ofFloat(1f, 0f);

            // Sets the animation properties
            animation.setDuration(300);
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
                    otherContent.startAnimation(anim_fade_in);

                    // Declaring animator
                    ValueAnimator nextanimation = ValueAnimator.ofFloat(1f, 0f);

                    // Sets the animation properties
                    nextanimation.setDuration(300);
                    nextanimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (float) animation.getAnimatedValue();

                            // Sets weight of the two layouts, this makes one smaller and the other bigger
                            tableRowTopHalf.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, value));
                            otherContent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1 - value));
                        }
                    });

                    nextanimation.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            otherContentHandler(view); // Takes care of including new views
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    nextanimation.start();
                    pullUp = true; // Changing the pull up status indicator
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
        else {
            pullDown(); // If there is a layout already pulled up we have to pull it down
        }
    }

    // Same as smallButtonPressed just backwards
    public void pullDown() {
        // Declaring animator
        ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);

        // Sets the animation properties
        animation.setDuration(300);
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
                otherContent.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                contentLinear.setVisibility(View.VISIBLE);
                otherContent.removeAllViews();

                // Declaring animator
                ValueAnimator nextanimation = ValueAnimator.ofFloat(0f, 1f);

                // Sets the animation properties
                nextanimation.setDuration(300);
                nextanimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        contentLinear.setAlpha(value);
                    }
                });
                nextanimation.start();
                pullUp = false;
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

    // Android back key function
    @Override
    public void onBackPressed() {
        Log.i("SuPark", "Back pressed.");

        // If the dimming layer is visible then makes invisible, otherwise
        // triggers the default action of back button.
        if (dimActive) {
            // Deactivates the dimming
            dimBackground(false);
        }
        else if (pullUp) {
            // If there is an activity pulled up, pulls down
            pullDown();
        }
        else {
            // Default action
            finish();
        }
    }

}

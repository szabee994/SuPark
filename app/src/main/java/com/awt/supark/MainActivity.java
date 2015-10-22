package com.awt.supark;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity  {
    boolean dimActive = false;  // Holds the dim layers status (true = visible, false = invisible/gone)

    // Declaring the animation variables
    Animation anim_fade_in;
    Animation anim_fade_out;
    Animation anim_zoom_in;
    Animation anim_zoom_out;
    ImageButton btnPark;
    RelativeLayout backDimmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Declaring the animations
        anim_zoom_in = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        anim_zoom_out = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        anim_fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        anim_fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        // Declaring the UI elements
        btnPark = (ImageButton) findViewById(R.id.buttonPark);
        backDimmer = (RelativeLayout) findViewById(R.id.back_dimmer);

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

                // TODO:
                // * Bring the parking button above any other
                //   layers (at the current state it is below
                //   the dimming layer).
                //
                // * Write the circle buttons animation which
                //   bring it to the top center of the screen
                //   and makes it a bit larger.

            }
        });
    }

    // Background dimming function (true = dimmed, false = normal)
    public void dimBackground(boolean turnOn) {
        if(turnOn == true) {
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

    // Android back key function
    @Override
    public void onBackPressed() {
        Log.i("SuPark", "Back pressed.");

        // If the dimming layer is visible then makes invisible, otherwise
        // triggers the default action of back button.
        if(dimActive) {
            // Deactivates the dimming
            dimBackground(false);
        }
        else {
            // Default action
            finish();
        }
    }

}

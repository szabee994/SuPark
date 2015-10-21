package com.awt.supark;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity  {

    @Override
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        Log.i("SuPark", "Back pressed.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Declaring the animations
        final Animation anim_zoom_in = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        final Animation anim_zoom_out = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        final Animation anim_fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        final Animation anim_fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        // Declaring the ui elements
        final ImageButton btnPark = (ImageButton) findViewById(R.id.buttonPark);
        final RelativeLayout backDimmer = (RelativeLayout) findViewById(R.id.back_dimmer);

        // Setting up a listener to track the touch/release events for the parking button
        btnPark.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                // If the user taps the button zooming in animation starts.
                // The animation will remain in it's final phase (pressed).
                if (motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    // Do the animation
                    view.startAnimation(anim_zoom_out);
                    anim_zoom_out.setFillAfter(true);
                }

                // If the user releases the button zooming out animation starts.
                // The animation will remain in it's final phase (released).
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
                backDimmer.setVisibility(View.VISIBLE);
                backDimmer.startAnimation(anim_fade_in);
            }
        });


    }

}

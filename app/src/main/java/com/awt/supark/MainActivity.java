package com.awt.supark;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Declaring the animations
        final Animation anim_zoom_in = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        final Animation anim_zoom_out = AnimationUtils.loadAnimation(this, R.anim.zoom_out);

        // Declaring the ui elements
        final ImageButton btnPark = (ImageButton) findViewById(R.id.buttonPark);

        // Event listener which will listen to the circle button's touch events
        btnPark.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                // If the user taps the button zooming in animation starts.
                // The animation will remain in it's final phase (pressed).
                if (motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    view.startAnimation(anim_zoom_out);
                    anim_zoom_in.setFillAfter(true);
                }

                // If the user releases the button zooming out animation starts.
                // The animation will remain in it's final phase (released).
                else if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
                    view.startAnimation(anim_zoom_in);
                    anim_zoom_out.setFillAfter(true);
                }

                // This is required for some reasons.
                return false;
            }
        });
    }
}

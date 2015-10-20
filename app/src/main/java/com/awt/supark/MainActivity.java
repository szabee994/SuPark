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

        final Animation anim_zoom_in = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        final Animation anim_zoom_out = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        final ImageButton btnPark = (ImageButton) findViewById(R.id.buttonPark);

        btnPark.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    view.startAnimation(anim_zoom_out);
                    anim_zoom_in.setFillAfter(true);
                }
                else if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
                    view.startAnimation(anim_zoom_in);
                    anim_zoom_out.setFillAfter(true);
                }
                return false;
            }
        });
    }
}

package com.awt.supark;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TableRow;

/**
 * Created by doctor on 24/11/2015.
 */
public class LayoutHandler {
    Context context;
    public LayoutHandler(Context cont){
        context = cont;
    }

    public void parkingBackgroundShow(final MainActivity act) {
        if(act.dimActive) {  // Only works when the dimming layer is visible
            act.parkingBackground.setVisibility(View.VISIBLE);  // Turns on the parking background layout
            act.parkingBackground.startAnimation(act.anim_center_open_up);  // Animates the parking background layout
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

    public void updateLocationTextButton(final MainActivity act) {
        switch(act.currentZone) {
            case 1:
                act.locationInfo.setText(act.getResources().getString(R.string.zone1selected));
                break;
            case 2:
                act.locationInfo.setText(act.getResources().getString(R.string.zone2selected));
                break;
            case 3:
                act.locationInfo.setText(act.getResources().getString(R.string.zone3selected));
                break;
            case 4:
                act.locationInfo.setText(act.getResources().getString(R.string.zone4selected));
                break;
        }
        act.locationLocked = true;
    }

    public void activeZoneButton(int zone, final MainActivity act) {
        float inactive = 0.3f;
        float active = 1.0f;

        switch (zone) {
            case 0:
                act.btnZone1.setAlpha(inactive);
                act.btnZone2.setAlpha(inactive);
                act.btnZone3.setAlpha(inactive);
                act.btnZone4.setAlpha(inactive);
                break;
            case 1:
                act.btnZone1.setAlpha(active);
                act.btnZone2.setAlpha(inactive);
                act.btnZone3.setAlpha(inactive);
                act.btnZone4.setAlpha(inactive);
                break;
            case 2:
                act.btnZone1.setAlpha(inactive);
                act.btnZone2.setAlpha(active);
                act.btnZone3.setAlpha(inactive);
                act.btnZone4.setAlpha(inactive);
                break;
            case 3:
                act.btnZone1.setAlpha(inactive);
                act.btnZone2.setAlpha(inactive);
                act.btnZone3.setAlpha(active);
                act.btnZone4.setAlpha(inactive);
                break;
            case 4:
                act.btnZone1.setAlpha(inactive);
                act.btnZone2.setAlpha(inactive);
                act.btnZone3.setAlpha(inactive);
                act.btnZone4.setAlpha(active);
                break;
        }

    }

    public void updateLocationTextGps(final MainActivity act) {
        if (!act.locationLocked) {
            if (act.locationFound) {
                //act.imageLocation.clearAnimation();  // Stops the blinking GPS image
                String text = "";
                switch (act.currentZone) {
                    case 0:
                        text = act.getResources().getString(R.string.nozone_auto);
                        break;
                    case 1:
                        text = act.getResources().getString(R.string.zone1auto);
                        break;
                    case 2:
                        text = act.getResources().getString(R.string.zone2auto);
                        break;
                    case 3:
                        text = act.getResources().getString(R.string.zone3auto);
                        break;
                    case 4:
                        text = act.getResources().getString(R.string.zone4auto);
                        break;
                }
                if (act.currentZone > 0) {
                    text = text + ": " + act.parkHandler.getRegionName(act.currentRegion);
                }
                act.locationInfo.setText(text);
            } else {
                act.imageLocation.startAnimation(act.anim_blink);
                act.locationInfo.setText(act.getResources().getString(R.string.locating));
            }
        }
    }

    // Background color switcher
    public void colorSwitch(int zone, final MainActivity act) {
        int fadeTime = 600;
        switch(zone) {
            case 1:
                appBackgroundColorChange(act.wrapper, fadeTime, 183, 28, 28);  // Color of red zone (RGB)
                break;
            case 2:
                appBackgroundColorChange(act.wrapper, fadeTime, 255, 160, 0);  // Color of yellow zone (RGB)
                break;
            case 3:
                appBackgroundColorChange(act.wrapper, fadeTime, 0, 121, 107);  // Color of green zone (RGB)
                break;
            case 4:
                appBackgroundColorChange(act.wrapper, fadeTime, 1, 87, 155);  // Color of blue zone (RGB)
                break;
        }
    }

    public void otherContentHandler(View view, final MainActivity act) {
        // Gets the pressed buttons ID
        switch (view.getId()) {
            case R.id.buttonMap:
                act.fragment = new MapFragment();
                break;
            case R.id.buttonStatistics:
                act.fragment = new StatsFragment();
                break;
            case R.id.buttonCars:
                act.fragment = new CarsFragment();
                break;
            case R.id.buttonEtc:
                act.fragment = new EtcFragment();
                break;
        }


        if (act.fragment != null) {
            FragmentTransaction fragmentTransaction = act.fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.fragment_fadein2, R.anim.fragment_fadeout2);
            fragmentTransaction.replace(R.id.otherContent, act.fragment);
            fragmentTransaction.commit();
            act.openedLayout = view.getId();
            //edit = false;
            // Sets the openedLayout variable so we know which one of the foreign layout was opened
        }
    }

    public void smallButtonPressed(final View view, final MainActivity act) {
        if (!act.pullUp && !act.pullUpStarted) { // If it isn't already up
            act.pullUpStarted = true;
            // Declaring animator
            ValueAnimator animation = ValueAnimator.ofFloat(1f, 0.17f);

            // ****** UI ELEMENTS FADING OUT ANIMATION ******
            // Sets the animation properties
            animation.setDuration(act.layoutFadeOutDuration);

            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();

                    // Fades out contentLinear and all buttons, except the one that is pressed
                    act.contentLinear.setAlpha(value);
                    if (view.getId() != R.id.buttonMap) {
                        act.btnMap.setAlpha(value);
                    }
                    if (view.getId() != R.id.buttonCars) {
                        act.btnCars.setAlpha(value);
                    }
                    if (view.getId() != R.id.buttonStatistics) {
                        act.btnStatistics.setAlpha(value);
                    }
                    if (view.getId() != R.id.buttonEtc) {
                        act.btnEtc.setAlpha(value);
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
                    act.contentLinear.setVisibility(View.GONE);
                    act.otherContent.setVisibility(View.VISIBLE);

                    // ****** BUTTON PULL UP ANIMATION ******
                    // Declaring animator
                    ValueAnimator nextAnimation = ValueAnimator.ofFloat(1f, 0f);

                    // Sets the animation properties
                    nextAnimation.setDuration(act.layoutPullUpDuration);
                    nextAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

                    nextAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (float) animation.getAnimatedValue();

                            // Sets weight of the two layouts, this makes one smaller and the other bigger
                            act.tableRowTopHalf.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, value));
                            act.otherContent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1 - value));
                        }
                    });

                    // ****** LAYOUT PULL UP ANIMATION ******
                    nextAnimation.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            act.otherContentHandler(view); // Takes care of including new views
                            //act.otherContent.startAnimation(act.anim_slide_up_fade_in); // Animates the new activity
                            act.pullUp = true; // Changing the pull up status indicator
                            act.pullUpStarted = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            act.pullUpStarted = false;
                            act.otherContent.setVisibility(View.GONE);
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
        else if (view.getId() == act.openedLayout){
            act.pullDown(); // If there is a layout already pulled up we have to pull it down
        }
        else if (act.pullUp && (act.openedLayout != 0) && !act.pullUpStarted) {
            act.pullUpStarted = true; // To prevent more than one highlight

            // Changing highlight from previous to current button
            ValueAnimator animation = ValueAnimator.ofFloat(0.17f, 1f);
            animation.setDuration(act.smallButtonHighlightChangeDuration);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    act.findViewById(view.getId()).setAlpha(value);
                    act.findViewById(act.openedLayout).setAlpha(1.17f - value);
                }
            });
            animation.start();

            animation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    act.otherContentHandler(view);  // Switches the layout to the new one
                    //act.otherContent.startAnimation(act.anim_slide_up_fade_in); // Fades in the new layout
                    act.pullUpStarted=false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            // Fades out current layout
            /*act.otherContent.startAnimation(act.anim_fade_out);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    act.otherContentHandler(view);  // Switches the layout to the new one
                    act.otherContent.startAnimation(act.anim_slide_up_fade_in); // Fades in the new layout
                    act.pullUpStarted=false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });*/
        }
    }

    // Same as smallButtonPressed just backwards
    public void pullDown(final MainActivity act) {
        act.animInProgress = true;

        // ****** BUTTON AND LAYOUT PULL DOWN ANIMATION
        // Declaring animator
        ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);

        // Sets the animation properties
        animation.setDuration(act.layoutPullUpDuration);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                act.tableRowTopHalf.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, value));
                act.btnMap.setAlpha(value);
                act.btnCars.setAlpha(value);
                act.btnStatistics.setAlpha(value);
                act.btnEtc.setAlpha(value);
            }
        });

        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Starts layout pull down and fade out animation
                act.otherContent.startAnimation(act.anim_slide_down_fade_out);
                act.anim_slide_down_fade_out.setFillAfter(true);

                // Makes layout invisible
                act.otherContent.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                act.contentLinear.setVisibility(View.VISIBLE);

                // ****** UI ELEMENTS FADE IN ANIMATION ******
                // Declaring animator
                ValueAnimator nextAnimation = ValueAnimator.ofFloat(0.17f, 1f);

                // Sets the animation properties
                nextAnimation.setDuration(act.layoutFadeInDuration);
                nextAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        act.contentLinear.setAlpha(value);
                    }
                });
                nextAnimation.start();

                act.pullUp = false;
                act.openedLayout = 0;
                act.animInProgress = false;
                FragmentTransaction transaction = act.fragmentManager.beginTransaction();
                transaction.remove(act.fragmentManager.findFragmentById(R.id.otherContent));
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


}

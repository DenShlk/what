package com.hypersphere.what.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.hypersphere.what.R;

/**
 *
 */
public class MenuController implements View.OnClickListener {

    public static final int ANIMATION_DURATION = 500;

    private final AnimatorSet animatorSet = new AnimatorSet();
    private final AnimationSet animationSet = new AnimationSet(false);  //i like these 2 lines
    private Activity activity;
    private View sheet;
    private Toolbar toolbar;
    private ImageView navigationIcon;
    private Interpolator interpolator;
    private int height, width;
    public boolean menuShown = false;
    private Drawable openIcon;
    private Drawable closeIcon;
    private View backgroundButton;

    MenuController(Activity activity, View sheet, Toolbar toolbar) {
        this(activity, sheet, toolbar, null);
    }

    MenuController(Activity activity, View sheet, Toolbar toolbar, @Nullable Interpolator interpolator) {
        this(activity, sheet, toolbar, interpolator, null, null);
    }

    public MenuController(
            Activity activity, View sheet, Toolbar toolbar, @Nullable Interpolator interpolator,
            @Nullable Drawable openIcon, @Nullable Drawable closeIcon) {
        this.activity = activity;
        this.sheet = sheet;
        this.interpolator = interpolator;
        this.openIcon = openIcon;
        this.closeIcon = closeIcon;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View v = toolbar.getChildAt(i);
            if(v instanceof ImageView){
                navigationIcon = (ImageView) v;
            }
        }

        backgroundButton = activity.findViewById(R.id.background_button);
        backgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuShown)
                   MenuController.this.onClick();
            }
        });
    }

    public void onClick(){
        onClick(null);
    }

    @Override
    public void onClick(View view) {
        menuShown = !menuShown;

        if(menuShown){
            //hide keyboard
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(backgroundButton.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

        }

        backgroundButton.setVisibility(menuShown ? View.VISIBLE : View.GONE);

        // Cancel the existing animations
        animatorSet.removeAllListeners();
        animatorSet.end();
        animatorSet.cancel();

        animationSet.cancel();

        updateIcon();

        final int translateX = (int) (width * 0.70);

        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(sheet, "translationX", menuShown ? translateX : 0);
        translationAnimator.setDuration(ANIMATION_DURATION);
        Animation scaleAnimation = AnimationUtils.loadAnimation(activity, menuShown ? R.anim.scale_down_animation : R.anim.scale_up_animation);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setDuration(ANIMATION_DURATION);
        if (interpolator != null) {
            translationAnimator.setInterpolator(interpolator);
            scaleAnimation.setInterpolator(interpolator);
        }


        sheet.startAnimation(scaleAnimation);
        animatorSet.play(translationAnimator);
        animationSet.addAnimation(scaleAnimation);
        scaleAnimation.start();
        translationAnimator.start();
    }

    private void updateIcon() {
        if (openIcon != null && closeIcon != null) {
            if (!(navigationIcon instanceof ImageView)) {
                throw new IllegalArgumentException("updateIcon() must be called on an ImageView");
            }
            if (menuShown) {
                navigationIcon.setImageDrawable(closeIcon);
            } else {
                navigationIcon.setImageDrawable(openIcon);
            }
        }
    }
}

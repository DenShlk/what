/*
 * Copyright 2020 Denis Shulakov
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.hypersphere.what.helpers;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.hypersphere.what.R;
import com.hypersphere.what.activities.MainActivity;

/**
 * Implements menu behavior in {@link MainActivity}.
 */
public class MenuHelper implements View.OnClickListener {

    public static final int ANIMATION_DURATION = 500;

    private final AnimatorSet animatorSet = new AnimatorSet();
	private final AnimationSet animationSet = new AnimationSet(false);
    private final Activity activity;
    private final View sheet;
    private ImageView navigationIcon;
    private final Interpolator interpolator;
    private int height, width;

	private boolean menuShown = false;
    private final Drawable openIcon;
    private final Drawable closeIcon;
    private final View backgroundButton;

	private MenuHelper(Activity activity, View sheet, Toolbar toolbar, @Nullable Interpolator interpolator) {
        this(activity, sheet, toolbar, interpolator, null, null);
    }

	public MenuHelper(
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
        backgroundButton.setOnClickListener(v -> {
            if(menuShown)
	            MenuHelper.this.onClick();
        });
    }

	public boolean isMenuShown() {
		return menuShown;
	}

    public void onClick(){
        onClick(null);
    }

	/**
	 * Implements onClick event on menu icon, starts transition and scale animations.
	 *
	 * @param view
	 */
	@Override
	public void onClick(View view) {
		menuShown = !menuShown;

		if(menuShown) {
			KeyboardHelper.hideKeyboard(backgroundButton);
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

	/**
	 * Replaces menu icon from open to close or close to open.
	 */
	private void updateIcon() throws IllegalArgumentException {
        if (openIcon != null && closeIcon != null) {
            if (navigationIcon == null) {
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

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

package com.hypersphere.what.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

/**
 * Custom ProgressBar with upgraded animations and resolution.
 * Usually we use ProgressBar.setMax to define maximum level of progress, BUT:
 * 1) if we set it as small value ProgressBar will looks awful and animations will have bad quality.
 * 2) progress can't be float value.
 * This class solve that problems.
 */
public class AnimatedProgressBar extends ProgressBar {
	public AnimatedProgressBar(Context context) {
		super(context);
		setMax(resolution);
	}

	public AnimatedProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		setMax(resolution);
	}

	public AnimatedProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setMax(resolution);
	}

	public AnimatedProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		setMax(resolution);
		setProgress(0);
	}

	private int resolution = 1000;
	private double progress = 0;
	private double maxProgress = 100;

	private long animationDuration = 2000;

	/**
	 * Returns current progress value from 0 to resolution value.
	 *
	 * @return
	 */
	private int getValue(){
		return (int) (resolution * progress / maxProgress);
	}

	public void setAnimationDuration(long animationDuration) {
		this.animationDuration = animationDuration;
	}

	/**
	 * Sets resolution and save progress at same level.
	 * @param newResolution
	 */
	public void setResolution(int newResolution) {
		setMax(newResolution);
		this.resolution = newResolution;
		setProgress(getValue());
	}

	/**
	 * Sets maximum progress value of ProgressBar.
	 *
	 * @param max
	 */
	public synchronized void setMaxProgress(double max) {
		progress = progress * max / maxProgress;
		maxProgress = max;
	}

	/**
	 * Sets current progress and animate it if need.
	 * @param progress
	 * @param animate Is animation required
	 */
	@Override
	public void setProgress(int progress, boolean animate) {
		setProgress(progress * 1.0, animate);
	}

	public void setProgress(double newProgress, boolean animate) {
		if(animate) {
			ProgressAnimation animation = new ProgressAnimation(this, progress, newProgress);
			animation.setDuration(animationDuration);
			setAnimation(animation);
		} else{
			progress = newProgress;
			super.setProgress(getValue());
		}
	}

	@Override
	public synchronized void setProgress(int progress) {
		setProgress(progress * 1.0, false);
	}

	public synchronized void setProgress(double progress) {
		this.progress = progress;
		super.setProgress(getValue(), false);
	}

	/**
	 * Animation between two values in progress bar.
	 */
	static class ProgressAnimation extends Animation {
		private final double from;
		private final double to;
		private final AnimatedProgressBar progressBar;

		private final static Interpolator defaultInterpolator = new LinearInterpolator();

		ProgressAnimation(AnimatedProgressBar progressBar, double from, double to, Interpolator interpolator){
			this.from = from;
			this.to = to;
			this.progressBar = progressBar;
			setInterpolator(interpolator);
		}

		ProgressAnimation(AnimatedProgressBar progressBar, double from, double to) {
			this(progressBar, from, to, defaultInterpolator);
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			super.applyTransformation(interpolatedTime, t);

			double value = from + (to - from) * getInterpolator().getInterpolation(interpolatedTime);
			progressBar.setProgress(value);
		}
	}
}

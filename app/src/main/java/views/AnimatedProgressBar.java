package views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

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
	}

	private int resolution = 1000;
	private double progress = 0;
	private double maxProgress = 100;

	private long animationDuration = 2000;

	private int getValue(){
		return (int) (resolution * progress / maxProgress);
	}

	public void setAnimationDuration(long animationDuration) {
		this.animationDuration = animationDuration;
	}

	public void setResolution(int newResolution) {
		setMax(newResolution);
		this.resolution = newResolution;
		setProgress(getValue());
	}

	public synchronized void setMax(double max) {
		progress = progress * max / maxProgress;
		maxProgress = max;
	}

	@Override
	public void setProgress(int progress, boolean animate) {
		setProgress(progress * 1.0, false);
	}

	@Override
	public synchronized void setProgress(int progress) {
		setProgress(progress * 1.0);
	}

	public synchronized void setProgress(double progress) {
		this.progress = progress;
		super.setProgress(getValue());
	}

	public void setProgress(double newProgress, boolean animate) {
		if(animate) {
			ProgressAnimation animation = new ProgressAnimation(this, progress, newProgress);
			animation.setDuration(animationDuration);
			setAnimation(animation);
		}
		else{
			progress = newProgress;
			super.setProgress(getValue());
		}
	}

	public static class ProgressAnimation extends Animation {
		private double from;
		private double to;
		private AnimatedProgressBar progressBar;

		public ProgressAnimation(AnimatedProgressBar progressBar, double from, double to) {
			this.from = from;
			this.to = to;
			this.progressBar = progressBar;
			setInterpolator(new LinearInterpolator());
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			super.applyTransformation(interpolatedTime, t);

			double value = from + (to - from) * getInterpolator().getInterpolation(interpolatedTime);
			progressBar.setProgress(value);
		}
	}
}

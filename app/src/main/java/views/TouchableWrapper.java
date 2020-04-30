package views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TouchableWrapper extends FrameLayout {
	public TouchableWrapper(@NonNull Context context) {
		super(context);
	}

	public TouchableWrapper(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchableWrapper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public TouchableWrapper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	private onCameraMoveEndListener listener;
	private int state = 0;
	private static final int STATE_BEFORE = 0;
	private static final int STATE_DOWN = 1;
	private static final int STATE_MOVE = 2;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		if(state == STATE_BEFORE && ev.getAction() == MotionEvent.ACTION_DOWN)
			state++;
		else{
			if(state==STATE_DOWN && ev.getAction() == MotionEvent.ACTION_MOVE)
				state++;
			else{
				if(state==STATE_MOVE && ev.getAction() == MotionEvent.ACTION_UP) {
					listener.onCameraMoveEnd();
					state = STATE_BEFORE;
				}
				else
					if(state==STATE_MOVE && ev.getAction()==MotionEvent.ACTION_MOVE) {}
					else
						state = STATE_BEFORE;
			}
		}
		Log.d("touch state", state + " " + ev.getAction());
		return super.dispatchTouchEvent(ev);
	}

	public void setListener(onCameraMoveEndListener listener) {
		this.listener = listener;
	}

	public interface onCameraMoveEndListener{
		public void onCameraMoveEnd();
	}
}

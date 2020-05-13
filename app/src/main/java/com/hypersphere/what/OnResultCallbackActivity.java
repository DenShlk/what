package com.hypersphere.what;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hypersphere.what.views.EditableGalleryRecyclerAdapter;

public abstract class OnResultCallbackActivity extends AppCompatActivity {


	private EditableGalleryRecyclerAdapter.ImageViewHolder waitForCallback;

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(waitForCallback!=null)
			waitForCallback.onResult(requestCode, resultCode, data);
	}

	public void setWaitForCallback(EditableGalleryRecyclerAdapter.ImageViewHolder waitForCallback) {
		this.waitForCallback = waitForCallback;
	}
}

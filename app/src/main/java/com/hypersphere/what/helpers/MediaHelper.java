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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import com.hypersphere.what.WhatApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

public class MediaHelper {

	private static final int MAX_IMAGE_SIZE = 1000 * 1000;

	/**
	 * Reads bitmap image by given uri.
	 *
	 * @param uri
	 * @return
	 */
	public static Bitmap getImageByUri(Uri uri) {
		try {
			ParcelFileDescriptor parcelFileDescriptor = WhatApplication.getContext().getContentResolver().openFileDescriptor(uri, "r");
			FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
			Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

			parcelFileDescriptor.close();

			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Check if this bitmap has more pixels than MAX_IMAGE_SIZE if so compresses to that size.
	 *
	 * @param bitmap
	 * @return byte array data of result bitmap
	 */
	public static byte[] bitmapToCompressedData(@NonNull Bitmap bitmap) {

		int size = bitmap.getWidth() * bitmap.getHeight();
		if (size > MAX_IMAGE_SIZE) {
			double k = Math.sqrt(1.0 * size / MAX_IMAGE_SIZE);
			bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() / k), (int) (bitmap.getHeight() / k), false);
		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

		return stream.toByteArray();
	}

	/**
	 * Saves given bitmap as given file
	 *
	 * @param bitmap
	 * @param file
	 */
	public static void saveBitmapToFile(Bitmap bitmap, File file) {
		try {
			FileOutputStream stream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			stream.flush();
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads bitmap from given file.
	 *
	 * @param file
	 * @return opened bitmap
	 */
	public static Bitmap readBitmapFromFile(File file) {
		return BitmapFactory.decodeFile(file.getPath());
	}
}

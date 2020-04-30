package com.hypersphere.what;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class for working with Firebase (or other CloudStorage)
 */

public class CloudManager {
	private static FirebaseDatabase database;
	private static DatabaseReference projectsRef, usersRef;

	private static StorageReference storage;
	private static StorageReference images;

	private static FirebaseAuth firebaseAuth;

	private static boolean started = false;

	private static final int MAX_IMAGE_SIZE = 1000 * 1000;
	private static Map<String, Bitmap> imageCash = new HashMap<>();

	private static void start(){
		firebaseAuth = FirebaseAuth.getInstance();
		firebaseAuth.signInAnonymously();

		database = FirebaseDatabase.getInstance();
		projectsRef = database.getReference("projects");
		usersRef = database.getReference("users");

		storage = FirebaseStorage.getInstance().getReference();
		images = storage.child("images");
	}

	public static void startIfNeed(){
		if(!started)
			start();
	}

	public static void newProject(ProjectEntry project, List<Bitmap> images, final OnUploadListener listener){
		project.images = new ArrayList<>();
		for(Bitmap bmp : images){
			project.images.add(newImage(bmp));
		}

		projectsRef.push().setValue(project.getJson()).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				listener.onComplete();
			}
		});
	}

	public static String newImage(Bitmap image){
		String imName = String.valueOf(UUID.randomUUID());
		images.child(imName).putBytes(bitmapToCompressedData(image)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
				task.getException();
			}
		});

		return imName;
	}

	public static void loadImage(final String src, final OnDownloadListener<Bitmap> listener){
		loadImage(src, listener, true);
	}

	public static void loadImage(final String src, final OnDownloadListener<Bitmap> listener, final boolean cashing){
		images.child(src).getBytes(10000000).addOnCompleteListener(new OnCompleteListener<byte[]>() {
			@Override
			public void onComplete(@NonNull Task<byte[]> task) {
				byte[] data  = task.getResult();
				Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
				listener.onComplete(image);

				if(cashing){
					imageCash.put(src, image);
				}
			}
		});
	}

	private static byte[] bitmapToCompressedData(Bitmap bmp){

		int size = bmp.getWidth() * bmp.getHeight();
		if(size > MAX_IMAGE_SIZE){
			double k = Math.sqrt(1.0 * size / MAX_IMAGE_SIZE);
			bmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() / k), (int) (bmp.getHeight() / k), false);
		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	public static void loadProjects(final OnDownloadListener<List<ProjectEntry>> listener){
		projectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				List<ProjectEntry> projects = new ArrayList<>();
				for(DataSnapshot snapshot : dataSnapshot.getChildren()){
					Gson gson = new Gson();
					projects.add(gson.fromJson(snapshot.getValue(String.class), ProjectEntry.class));
				}
				listener.onComplete(projects);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.e(getClass().getName(), databaseError.getMessage());
			}
		});
	}

	public interface OnDownloadListener<T>{
		void onComplete(T data);
	}

	public interface OnUploadListener{
		void onComplete();
	}
}

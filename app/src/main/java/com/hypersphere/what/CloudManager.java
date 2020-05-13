package com.hypersphere.what;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.model.UserEntry;

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

	private static final String YANDEX_WALLET_API_KEY = "6BC6EB098D661CCA8771C67A3141A63588E3D6CD7E8457A9815E891B7D3CDF8F";

	private static FirebaseDatabase database;
	private static DatabaseReference projectsRef, usersRef;

	private static StorageReference storage;
	private static StorageReference images;

	private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

	private static boolean userLoading = false;

	private static final int MAX_IMAGE_SIZE = 1000 * 1000;
	private static Map<String, Bitmap> imageCash = new HashMap<>();

	private static UserEntry curUser;

	private static void start() {
		if(!userLoading && curUser==null && firebaseAuth.getCurrentUser()!=null) {
			getUser(firebaseAuth.getCurrentUser().getUid(), new OnDownloadListener<UserEntry>() {
				@Override
				public void onComplete(UserEntry data) {
					curUser = data;
				}

				@Override
				public void onCancel() {}
			});
		}

		database = FirebaseDatabase.getInstance();
		projectsRef = database.getReference("projects");
		usersRef = database.getReference("users");

		storage = FirebaseStorage.getInstance().getReference();
		images = storage.child("images");
	}

	public static void newProject(ProjectEntry project, List<Bitmap> images, final OnUploadListener listener) {
		start();

		project.images = new ArrayList<>();
		for (Bitmap bmp : images) {
			project.images.add(newImage(bmp, null));
		}

		DatabaseReference newRef = projectsRef.push();
		project.id = newRef.getKey();
		curUser.myProjects.add(newRef.getKey());
		updateCurUser();

		newRef.setValue(project.getJson()).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				listener.onComplete();
			}
		});
	}

	public static void login(String email, String password, final OnAuthListener listener) {
		start();
		firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					getUser(firebaseAuth.getCurrentUser().getUid(), new OnDownloadListener<UserEntry>() {
						@Override
						public void onComplete(UserEntry data) {
							curUser = data;
						}

						@Override
						public void onCancel() {
						}
					});

					listener.onSuccess();
				} else
					listener.onError();
			}
		});
	}

	public static boolean isLoginNeed(){
		start();

		return firebaseAuth.getCurrentUser() == null;
	}

	public static void sign(final String username, final String email, String password, final OnAuthListener listener) {
		start();
		firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					curUser = new UserEntry(username, email, new ArrayList<String>(), firebaseAuth.getCurrentUser().getUid(), "default_avatar.png");
					updateCurUser();

					listener.onSuccess();
				}else
					listener.onError();
			}
		});
	}

	public static void updateCurUser(){
		Gson gson = new Gson();
		String userData = gson.toJson(getCurUser(), UserEntry.class);
		usersRef.child(firebaseAuth.getCurrentUser().getUid()).setValue(userData);
	}

	public static String newImage(Bitmap image, final OnUploadListener listener) {
		start();

		String imName = String.valueOf(UUID.randomUUID());
		images.child(imName).putBytes(bitmapToCompressedData(image)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
				if (listener != null)
					if (task.isSuccessful())
						listener.onComplete();
					else {
						task.getException().printStackTrace();
						listener.onCancel();
					}
			}
		});

		return imName;
	}

	public static void loadImage(final String src, final OnDownloadListener<Bitmap> listener) {
		loadImage(src, listener, true);
	}

	public static void loadImage(final String src, final OnDownloadListener<Bitmap> listener, final boolean cashing) {
		start();

		if(imageCash.containsKey(src)){
			listener.onComplete(imageCash.get(src));
		}else {
			images.child(src).getBytes(10000000).addOnCompleteListener(new OnCompleteListener<byte[]>() {
				@Override
				public void onComplete(@NonNull Task<byte[]> task) {
					if (!task.isSuccessful())
						loadImage(src, listener, cashing);

					byte[] data = task.getResult();
					Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
					listener.onComplete(image);

					if (cashing) {
						imageCash.put(src, image);
					}
				}
			});
		}
	}

	public static void getUser(String uid, final OnDownloadListener<UserEntry> listener) {
		userLoading = true;
		start();

		usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userLoading = false;
				Gson gson = new Gson();
				listener.onComplete(gson.fromJson(dataSnapshot.getValue(String.class), UserEntry.class));
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				userLoading = false;
				databaseError.toException().printStackTrace();
				listener.onCancel();
			}
		});
	}

	private static byte[] bitmapToCompressedData(Bitmap bmp) {

		int size = bmp.getWidth() * bmp.getHeight();
		if (size > MAX_IMAGE_SIZE) {
			double k = Math.sqrt(1.0 * size / MAX_IMAGE_SIZE);
			bmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() / k), (int) (bmp.getHeight() / k), false);
		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	public static void loadProjects(final OnDownloadListener<List<ProjectEntry>> listener) {
		start();

		projectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				List<ProjectEntry> projects = new ArrayList<>();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					Gson gson = new Gson();
					projects.add(gson.fromJson(snapshot.getValue(String.class), ProjectEntry.class));
				}
				listener.onComplete(projects);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.e(getClass().getName(), databaseError.getMessage());
				loadProjects(listener);
			}
		});
	}

	public static UserEntry getCurUser() {
		return curUser;
	}

	public static ProjectEntry notifyDonation(ProjectEntry project, double payAmount) {
		project.donationsCollected += payAmount;
		// TODO: 14.05.2020 project finished
		Gson gson = new Gson();
		projectsRef.child(project.id).setValue(gson.toJson(project));
		return project;
	}

	public interface OnAuthListener {
		void onSuccess();

		void onError();
	}

	public interface OnDownloadListener<T> {
		void onComplete(T data);

		void onCancel();
	}

	public interface OnUploadListener {
		void onComplete();

		void onCancel();
	}
}

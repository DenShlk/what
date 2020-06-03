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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.hypersphere.what.model.CommentEntry;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.model.UserEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class for working with Firebase Authentication, Realtime Database, Storage.
 */

public class CloudHelper {

	private static DatabaseReference projectsRef, usersRef, commentsRef, projectsDoneNotificationRef, projectsDoneRef;

	private static StorageReference images;

	private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

	private static boolean currentUserLoading = false;

	private static OnDownloadListener<UserEntry> userDownloadListener;

	private static final Map<String, Bitmap> imageCash = new HashMap<>();

	private static UserEntry curUser;
	
	private static final Gson gson = new Gson();

	/**
	 * initialization
	 */
	static {
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		projectsRef = database.getReference("projects");
		usersRef = database.getReference("users");
		commentsRef = database.getReference("comments");
		projectsDoneRef = database.getReference("projects-done");
		projectsDoneNotificationRef = database.getReference("projects-done-notifications");

		StorageReference storage = FirebaseStorage.getInstance().getReference();
		images = storage.child("images");

		if (!currentUserLoading && curUser == null && firebaseAuth.getCurrentUser() != null) {
			currentUserLoading = true;
			getUser(firebaseAuth.getCurrentUser().getUid(), new OnDownloadListener<UserEntry>() {
				@Override
				public void onComplete(UserEntry data) {
					currentUserLoading = false;
					curUser = data;
					if(userDownloadListener != null)
						userDownloadListener.onComplete(curUser);
					userDownloadListener = null;
				}

				@Override
				public void onCancel() {}
			});
		}
	}

	/**
	 * if current user have loaded it directly call onComplete of listener, otherwise it will be
	 * when user is loaded
	 *
	 * @param userDownloadListener
	 */
	public static void setCurrentUserDownloadListener(OnDownloadListener<UserEntry> userDownloadListener) {
		if (currentUserLoading)
			CloudHelper.userDownloadListener = userDownloadListener;
		else
			userDownloadListener.onComplete(getCurUser());
	}

	/**
	 * Loads project as new from current user to database
	 *
	 * @param project
	 * @param images  images of project it will be sent to Storage and added to project.images as ID-string
	 */
	public static void newProject(ProjectEntry project, List<Bitmap> images) {
		newProject(project, images, null);
	}

	public static void newProject(ProjectEntry project, List<Bitmap> images, @Nullable final OnUploadListener listener) {
		project.images = new ArrayList<>();
		for (Bitmap bmp : images) {
			project.images.add(newImage(bmp, null));
		}

		DatabaseReference newRef = projectsRef.push();
		project.id = newRef.getKey();
		curUser.myProjects.add(newRef.getKey());
		updateCurUser();

		String data = gson.toJson(project);
		Task<Void> task = newRef.setValue(data);
		if (listener != null)
			task.addOnCompleteListener(task1 -> {
				if (task1.isSuccessful())
					listener.onComplete();
				else
					listener.onCancel();
			});
	}

	/**
	 * login as user with given email and password
	 *
	 * @param email
	 * @param password
	 */
	public static void login(String email, String password){
		login(email, password, null);
	}

	public static void login(String email, String password, final OnAuthListener listener) {
		firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				getUser(firebaseAuth.getCurrentUser().getUid(), new OnDownloadListener<UserEntry>() {
					@Override
					public void onComplete(UserEntry data) {
						curUser = data;
						if (listener != null)
							listener.onSuccess();
					}

					@Override
					public void onCancel() {
						if (listener != null) listener.onError();
					}
				});
			} else if (listener != null) {
				listener.onError();
			}
		});
	}

	/**
	 * Creates new user with given username, email, password
	 *
	 * @param username
	 * @param email
	 * @param password
	 */
	public static void sign(final String username, final String email, final String password) {
		sign(username, email, password, null);
	}

	public static void sign(final String username, final String email, final String password, final OnAuthListener listener) {
		firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				curUser = new UserEntry(username, email, new ArrayList<>(), firebaseAuth.getCurrentUser().getUid(), "default_avatar.png");
				updateCurUser();

				if (listener != null)
					listener.onSuccess();
			} else if (listener != null)
				listener.onError();
		});
	}

	/**
	 * check if user already logged
	 *
	 * @return
	 */
	public static boolean isLoginNeed() {
		return firebaseAuth.getCurrentUser() == null;
	}

	/**
	 * Replaces current user database-value with current value
	 */
	private static void updateCurUser(){
		String userData = gson.toJson(getCurUser(), UserEntry.class);
		usersRef.child(firebaseAuth.getCurrentUser().getUid()).setValue(userData);
	}

	/**
	 * Creates id for new image and load it to images reference
	 *
	 * @param image
	 * @return id of image that can be used to download image
	 */
	public static String newImage(Bitmap image) {
		return newImage(image, null);
	}

	public static String newImage(Bitmap image, @Nullable final OnUploadListener listener) {
		String imageName = String.valueOf(UUID.randomUUID());
		images.child(imageName).putBytes(MediaHelper.bitmapToCompressedData(image)).addOnCompleteListener(task -> {
			if (listener != null)
				if (task.isSuccessful())
					listener.onComplete();
				else {
					task.getException().printStackTrace();

					listener.onCancel();
				}
		});

		return imageName;
	}

	/**
	 * Load image by src
	 * @param src id of image
	 * @param listener
	 */
	public static void loadImage(final String src, @NonNull final OnDownloadListener<Bitmap> listener) {
		if(imageCash.containsKey(src)){
			listener.onComplete(imageCash.get(src));
		}else {
			images.child(src).getBytes(10000000).addOnCompleteListener(task -> {
				if (!task.isSuccessful())
					loadImage(src, listener);

				byte[] data = task.getResult();
				Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
				listener.onComplete(image);

				imageCash.put(src, image);
			});
		}
	}

	/**
	 * Get user with given uid
	 * @param uid
	 * @param listener
	 */
	public static void getUser(String uid, @NonNull final OnDownloadListener<UserEntry> listener) {
		usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				listener.onComplete(gson.fromJson(dataSnapshot.getValue(String.class), UserEntry.class));
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				currentUserLoading = false;
				databaseError.toException().printStackTrace();
			}
		});
	}

	/**
	 * Load created projects from database
	 * @param listener
	 */
	public static void loadProjects(@NonNull final OnDownloadListener<List<ProjectEntry>> listener) {
		projectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				List<ProjectEntry> projects = new ArrayList<>();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					projects.add(gson.fromJson(snapshot.getValue(String.class), ProjectEntry.class));
				}
				listener.onComplete(projects);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.e(getClass().getName(), databaseError.getMessage());
				listener.onCancel();
			}
		});
	}

	/**
	 * Load done projects from database reference
	 * @param listener
	 */
	public static void loadDoneProjects(@NonNull final OnDownloadListener<List<ProjectEntry>> listener) {
		projectsDoneRef.child(curUser.id).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				List<ProjectEntry> projects = new ArrayList<>();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					projects.add(gson.fromJson(snapshot.getValue(String.class), ProjectEntry.class));
				}
				listener.onComplete(projects);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.e(getClass().getName(), databaseError.getMessage());
				listener.onCancel();
			}
		});
	}

	/**
	 * Load project by given id
	 * @param projectId
	 * @param listener
	 */
	public static void loadProject(String projectId, @NonNull OnDownloadListener<ProjectEntry> listener){
		projectsRef.child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				listener.onComplete(gson.fromJson(dataSnapshot.getValue(String.class), ProjectEntry.class));
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				databaseError.toException().printStackTrace();
				listener.onCancel();
			}
		});
	}

	/**
	 * Load done project by given id
	 * @param projectId
	 * @param listener
	 */
	public static void loadProjectDone(String projectId, @NonNull OnDownloadListener<ProjectEntry> listener){
		projectsDoneRef.child(curUser.id).child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				listener.onComplete(gson.fromJson(dataSnapshot.getValue(String.class), ProjectEntry.class));
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				databaseError.toException().printStackTrace();
				listener.onCancel();
			}
		});
	}

	/**
	 * @return current user
	 */
	public static UserEntry getCurUser() {
		return curUser;
	}

	/**
	 * Update project donationsCollected field and check if it reached donationsGoal calls
	 * finishProject
	 * @param project
	 * @param payAmount
	 * @return
	 */
	public static ProjectEntry notifyDonation(ProjectEntry project, double payAmount) {
		project.donationsCollected += payAmount;

		projectsRef.child(project.id).setValue(gson.toJson(project));

		if (project.donationsCollected - 0.01 >= project.donationsGoal) {
			finishProject(project);
		}

		return project;
	}

	/**
	 * Moves project from projects to projectsDone
	 * @param project
	 */
	public static void finishProject(ProjectEntry project){
		getUser(project.creatorId, new OnDownloadListener<UserEntry>() {
			@Override
			public void onComplete(UserEntry data) {

				projectsRef.child(project.id).removeValue();
				projectsDoneRef.child(project.creatorId).child(project.id).setValue(gson.toJson(project)).addOnCompleteListener(task -> projectsDoneNotificationRef.child(data.id).push().setValue(project.id));
				data.myProjects.remove(project.id);
				usersRef.child(project.creatorId).setValue(data);
			}

			@Override
			public void onCancel() {}
		});
	}

	/**
	 * Send given comment to database
	 * @param comment
	 */
	public static void newComment(CommentEntry comment) {
		String data = gson.toJson(comment);
		commentsRef.child(comment.projectId).push().setValue(data);
	}

	/**
	 * Change image-src of current user to given
	 *
	 * @param image
	 */
	public static void setUserImage(@NonNull Bitmap image) {
		curUser.image = newImage(image);
		updateCurUser();
	}

	/**
	 * Load comments by given projectId
	 * @param projectId
	 * @param listener
	 */
	public static void loadComments(String projectId, @NonNull OnDownloadListener<List<CommentEntry>> listener){
		commentsRef.child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				List<CommentEntry> list = new ArrayList<>();

				for(DataSnapshot commentData : dataSnapshot.getChildren()){
					list.add(gson.fromJson(commentData.getValue(String.class), CommentEntry.class));
				}
				listener.onComplete(list);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				listener.onCancel();
				databaseError.toException().printStackTrace();
			}
		});
	}

	/**
	 * Add listener to done projects database reference
	 * @param listener
	 */
	public static void listenDoneProjects(OnProjectDoneListener listener){
		projectsDoneNotificationRef.child(curUser.id).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(dataSnapshot.getChildrenCount() > 0) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						String projectDoneId = (String) snapshot.getValue();
						listener.onProjectDone(projectDoneId);
					}
					projectsDoneNotificationRef.child(curUser.id).removeValue();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				databaseError.toException().printStackTrace();
			}
		});
	}

	/**
	 * Listen project done event
	 */
	public interface OnProjectDoneListener {
		void onProjectDone(String projectId);
	}

	/**
	 * Listen authentication events
	 */
	public interface OnAuthListener {
		void onSuccess();

		void onError();
	}

	/**
	 * Listen download from database events
	 * @param <T> type of object
	 */
	public interface OnDownloadListener<T> {
		void onComplete(T data);

		void onCancel();
	}

	/**
	 * Listen upload to database events
	 */
	public interface OnUploadListener {
		void onComplete();

		void onCancel();
	}
}


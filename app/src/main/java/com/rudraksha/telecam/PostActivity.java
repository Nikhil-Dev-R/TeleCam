package com.rudraksha.telecam;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.hendraanggrian.appcompat.socialview.autocomplete.Hashtag;
import com.hendraanggrian.appcompat.socialview.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;
import com.rudraksha.telecam.databinding.ActivityPostBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {
	ActivityPostBinding binding;
	Uri imageUri;
	String imageUrl;
	SocialAutoCompleteTextView description;

	ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
		if (result.isSuccessful()) {
			Bitmap cropped = BitmapFactory.decodeFile(result.getUriFilePath(getApplicationContext(), true));
			saveCroppedImage(cropped);
		}
	});

	ActivityResultLauncher<Intent> getImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if (result.getResultCode() == Activity.RESULT_OK) {
			Intent data = result.getData();
			if (data != null && data.getData() != null) {
				imageUri = data.getData();
				launchImageCropper(imageUri);
			}
		}
	});

	private final ActivityResultLauncher<String> requestPermission = registerForActivityResult(
			new ActivityResultContracts.RequestPermission(), isGranted -> {
		if (isGranted) {
			getImageFile();
		} else {
			permissionDenied();
		}
	});

	ActivityResultLauncher<Intent> android11StoragePermission = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if (isPermitted()) {
			getImageFile();
		} else {
			permissionDenied();
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityPostBinding.inflate(getLayoutInflater());
		View view = binding.getRoot();
		setContentView(view);

		binding.close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(PostActivity.this, MainActivity.class));
				finish();
			}
		});

		binding.selectImage.setOnClickListener(v -> {
			if (isPermitted()) {
				getImageFile();
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
					requestAndroid11StoragePermission();
				} else {
					requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
				}
			}
		});

		binding.post.setOnClickListener(v -> upload());
	}

	private void getImageFile() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		getImage.launch(intent);
	}

	@TargetApi(Build.VERSION_CODES.R)
	private void requestAndroid11StoragePermission() {
		Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
		android11StoragePermission.launch(intent);
	}

	private boolean isPermitted() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return Environment.isExternalStorageManager();
		} else {
			return ContextCompat.checkSelfPermission(getApplicationContext(),
					Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
					ContextCompat.checkSelfPermission(getApplicationContext(),
							Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		}
	}

	private void launchImageCropper(Uri uri) {
		CropImageOptions cropImageOptions = new CropImageOptions();
		cropImageOptions.imageSourceIncludeGallery = false;
		cropImageOptions.imageSourceIncludeCamera = true;
		CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(uri, cropImageOptions);
		cropImage.launch(cropImageContractOptions);
	}

	private void permissionDenied() {
		Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_LONG).show();
	}

	private void showFailureMessage() {
		Toast.makeText(getApplicationContext(), "Cropped image not saved something went wrong", Toast.LENGTH_LONG).show();
	}

	private void showSuccessMessage() {
		Toast.makeText(getApplicationContext(), "Image Saved", Toast.LENGTH_LONG).show();
	}

	private void saveCroppedImage(Bitmap bitmap) {
		String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
		File myDir = new File(root + "/Cropped Images");

		if (!myDir.exists()) {
			myDir.mkdirs();
		}

		// Generate a unique file name
		String imageName = "Image_" + new Date().getTime() + "." + "jpg";

		File file = new File(myDir, imageName);
		if (file.exists()) file.delete();

		try {
			// Save the Bitmap to the file
			OutputStream outputStream;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				outputStream = Files.newOutputStream(file.toPath());
			} else {
				outputStream = new FileOutputStream(file);
			}
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			outputStream.flush();
			outputStream.close();

			// Add the image to the MediaStore
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
			values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + "jpeg");
			getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

			// Trigger a media scan to update the gallery
			MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getAbsolutePath()}, null, null);
			showSuccessMessage();
		} catch (Exception e) {
			showFailureMessage();
		}

		binding.imageAdded.setImageBitmap(bitmap);
	}

	private String getFileExtension(Uri uri) {
		return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
	}

	private void upload() {
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage("Uploading");
		pd.show();

		if (imageUri != null){
			final StorageReference filePath = FirebaseStorage.getInstance().getReference("Posts")
					.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

			StorageTask<UploadTask.TaskSnapshot> uploadTask = filePath.putFile(imageUri);
			// Handle success and failure of image upload
			uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
					// Image uploaded successfully
					Task<Uri> downloadUrlTask = filePath.getDownloadUrl();
					downloadUrlTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
						@Override
						public void onSuccess(Uri uri) {
							// Get the download URL
							imageUrl = uri.toString();

							DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
							String postId = ref.push().getKey();

							HashMap<String, Object> map = new HashMap<>();
							map.put("postid", postId);
							map.put("imageurl", imageUrl);
							map.put("description", description.getText().toString());
							map.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

							ref.child(postId).setValue(map);

							DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
							List<String> hashTags = description.getHashtags();
							if (!hashTags.isEmpty()) {
								for (String tag : hashTags) {
									map.clear();

									map.put("tag", tag.toLowerCase());
									map.put("postid", postId);

									mHashTagRef.child(tag.toLowerCase()).child(postId).setValue(map);
								}
							}

							// Dismiss progress dialog and navigate to main activity
							pd.dismiss();
							startActivity(new Intent(PostActivity.this, MainActivity.class));
							finish();
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							// Handle failures in getting download URL
							Toast.makeText(PostActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
							pd.dismiss();
						}
					});
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@androidx.annotation.NonNull Exception e) {
					Toast.makeText(PostActivity.this, "Failed to upload Image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
					pd.dismiss();
				}
			});
		} else {
			Toast.makeText(this, "No image was selected!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		final ArrayAdapter<Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getApplicationContext());

		FirebaseDatabase.getInstance().getReference().child("HashTags")
				.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					hashtagAdapter.add(new Hashtag(snapshot.getKey() , (int) snapshot.getChildrenCount()));
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		description.setHashtagAdapter(hashtagAdapter);
	}
}

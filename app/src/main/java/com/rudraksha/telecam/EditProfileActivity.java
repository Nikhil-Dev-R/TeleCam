package com.rudraksha.telecam;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rudraksha.telecam.Model.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

	private ImageView close;
	private CircleImageView imageProfile;
	private TextView save;
	private TextView changePhoto;
	private TextInputEditText fullname;
	private TextInputEditText username;
	private TextInputEditText bio;

	private FirebaseUser fUser;

	private Uri mImageUri;
	private StorageTask uploadTask;
	private StorageReference storageRef;

//	private ActivityResultLauncher<CropImageContractOptions> cropImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);

		close = findViewById(R.id.close);
		imageProfile = findViewById(R.id.image_profile);
		save = findViewById(R.id.save);
		changePhoto = findViewById(R.id.change_photo);
		fullname = findViewById(R.id.fullname);
		username = findViewById(R.id.username);
		bio = findViewById(R.id.bio);

		fUser = FirebaseAuth.getInstance().getCurrentUser();
		storageRef = FirebaseStorage.getInstance().getReference().child("Uploads");

		/*cropImage = registerForActivityResult(new CropImageContract(), result -> {
			if (result.isSuccessful()) {
				// Use the returned uri.
				Uri uriContent = result.getUriContent();
				String uriFilePath = result.getUriFilePath(this, true); // optional usage
			} else {
				// An error occurred.
				Exception exception = result.getError();
			}
		});*/

		FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				User user = dataSnapshot.getValue(User.class);
				fullname.setText(user.getName());
				username.setText(user.getUsername());
				bio.setText(user.getBio());
				Picasso.get().load(user.getImageurl()).into(imageProfile);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		/*changePhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cropImage.launch(
						new CropImageContractOptions(null, new CropImageOptions()
								.setCropShape(CropImageView.CropShape.OVAL))
				);
			}
		});

		imageProfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(EditProfileActivity.this);
			}
		});*/

		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateProfile();
			}
		});
	}

	private void updateProfile() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("fullname", fullname.getText().toString());
		map.put("username", username.getText().toString());
		map.put("bio", bio.getText().toString());

		FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).updateChildren(map);
	}

	private void uploadImage() {
		/*final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage("Uploading");
		pd.show();

		if (mImageUri != null) {
			final StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpeg");

			uploadTask = fileRef.putFile(mImageUri);
			uploadTask.continueWithTask(new Continuation() {
				@Override
				public Object then(@NonNull Task task) throws Exception {
					if (!task.isSuccessful()) {
						throw task.getException();
					}

					return  fileRef.getDownloadUrl();
				}
			}).addOnCompleteListener(new OnCompleteListener<Uri>() {
				@Override
				public void onComplete(@NonNull Task<Uri> task) {
					if (task.isSuccessful()) {
						Uri downloadUri = task.getResult();
						String url = downloadUri.toString();

						FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).child("imageurl").setValue(url);
						pd.dismiss();
					} else {
						Toast.makeText(EditProfileActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show();
					}
				}
			});
		} else {
			Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
		}*/
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		/*if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
			CropImage.ActivityResult result = CropImage.getActivityResult(data);
			mImageUri = result.getUri();

			uploadImage();
		} else {
			Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
		}*/
	}
}

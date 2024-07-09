package com.rudraksha.telecam.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rudraksha.telecam.Adapter.PhotoAdapter;
import com.rudraksha.telecam.EditProfileActivity;
import com.rudraksha.telecam.FollowersActivity;
import com.rudraksha.telecam.Model.Post;
import com.rudraksha.telecam.Model.User;
import com.rudraksha.telecam.OptionsActivity;
import com.rudraksha.telecam.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

	String profileId;
	private RecyclerView recyclerViewSaves;
	private PhotoAdapter postAdapterSaves;
	private List<Post> mySavedPosts;
	private RecyclerView recyclerView;
	private PhotoAdapter photoAdapter;
	private List<Post> myPhotoList;
	private CircleImageView imageProfile;
	private ImageView options;
	private TextView followers;
	private TextView following;
	private TextView posts;
	private TextView fullname;
	private TextView bio;
	private TextView username;
	private ImageView myPictures;
	private ImageView savedPictures;
	private ImageView badgeBeginner;
	private ImageView badgeMaster;
	private ImageView badgeHero;
	private ImageView badgeLegend;
	private ImageView badgeSupreme;
	private ImageView badgeFavourite;
	private Button editProfile;
	private FirebaseUser fUser;

	private void Badges(View view) {
		badgeBeginner = view.findViewById(R.id.badge_beginner);
		badgeMaster = view.findViewById(R.id.badge_master);
		badgeHero = view.findViewById(R.id.badge_hero);
		badgeLegend = view.findViewById(R.id.badge_legend);
		badgeSupreme = view.findViewById(R.id.badge_supreme);
	}

	private void Icons(View view) {
		options = view.findViewById(R.id.options);
		myPictures = view.findViewById(R.id.my_pictures);
		savedPictures = view.findViewById(R.id.saved_pictures);
	}

	private void Texts(View view) {
		followers = view.findViewById(R.id.followers);
		following = view.findViewById(R.id.following);
		posts = view.findViewById(R.id.posts);
		fullname = view.findViewById(R.id.fullname);
		bio = view.findViewById(R.id.bio);
		username = view.findViewById(R.id.username);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_profile, container, false);

		fUser = FirebaseAuth.getInstance().getCurrentUser();

		String data = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId", "none");

		if (data.equals("none")) {
			profileId = fUser.getUid();
		} else {
			profileId = data;
			getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().clear().apply();
		}

		Texts(view);
		Icons(view);
		Badges(view);

		imageProfile = view.findViewById(R.id.image_profile);
		editProfile = view.findViewById(R.id.edit_profile);

		recyclerView = view.findViewById(R.id.recycler_view_pictures);
		recyclerView.setHasFixedSize(true);
		// recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
		myPhotoList = new ArrayList<>();
		photoAdapter = new PhotoAdapter(getContext(), myPhotoList);
		recyclerView.setAdapter(photoAdapter);

		recyclerViewSaves = view.findViewById(R.id.recycler_view_saved);
		recyclerViewSaves.setHasFixedSize(true);
		recyclerViewSaves.setLayoutManager(new GridLayoutManager(getContext(), 4));
		mySavedPosts = new ArrayList<>();
		postAdapterSaves = new PhotoAdapter(getContext(), mySavedPosts);
		recyclerViewSaves.setAdapter(postAdapterSaves);

		userInfo();
		getFollowersAndFollowingCount();
		getPostCount();
		myPhotos();
		getSavedPosts();

		if (profileId.equals(fUser.getUid())) {
			editProfile.setText("Edit profile");
		} else {
			checkFollowingStatus();
		}

		editProfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String btnText = editProfile.getText().toString();

				if (btnText.equals("Edit profile")) {
					startActivity(new Intent(getContext(), EditProfileActivity.class));
				} else {
					if (btnText.equals("follow")) {
						FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
								.child("following").child(profileId).setValue(true);

						FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
								.child("followers").child(fUser.getUid()).setValue(true);
					} else {
						FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
								.child("following").child(profileId).removeValue();

						FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
								.child("followers").child(fUser.getUid()).removeValue();
					}
				}
			}
		});

		recyclerView.setVisibility(View.VISIBLE);
		recyclerViewSaves.setVisibility(View.GONE);

		myPictures.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				recyclerView.setVisibility(View.VISIBLE);
				recyclerViewSaves.setVisibility(View.GONE);
			}
		});

		savedPictures.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				recyclerView.setVisibility(View.GONE);
				recyclerViewSaves.setVisibility(View.VISIBLE);
			}
		});

		followers.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), FollowersActivity.class);
				intent.putExtra("id", profileId);
				intent.putExtra("title", "followers");
				startActivity(intent);
			}
		});

		following.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), FollowersActivity.class);
				intent.putExtra("id", profileId);
				intent.putExtra("title", "followings");
				startActivity(intent);
			}
		});

		options.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getContext(), OptionsActivity.class));
			}
		});

		return view;
	}

	private void getSavedPosts() {

		final List<String> savedIds = new ArrayList<>();

		FirebaseDatabase.getInstance().getReference().child("Saves").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					savedIds.add(snapshot.getKey());
				}

				FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
						mySavedPosts.clear();

						for (DataSnapshot snapshot1 : dataSnapshot1.getChildren()) {
							Post post = snapshot1.getValue(Post.class);

							for (String id : savedIds) {
								if (post.getPostid().equals(id)) {
									mySavedPosts.add(post);
								}
							}
						}

						postAdapterSaves.notifyDataSetChanged();
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {

					}
				});

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	private void myPhotos() {

		FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				myPhotoList.clear();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					Post post = snapshot.getValue(Post.class);

					if (post.getPublisher().equals(profileId)) {
						myPhotoList.add(post);
					}
				}

				Collections.reverse(myPhotoList);
				photoAdapter.notifyDataSetChanged();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	private void checkFollowingStatus() {

		FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
				.child("following").addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						if (dataSnapshot.child(profileId).exists()) {
							editProfile.setText("following");
						} else {
							editProfile.setText("follow");
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {

					}
				});

	}

	/*private void getPostCount() {
		FirebaseDatabase.getInstance().getReference().child("Posts").child(profileId)
		.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				posts.setText(String.valueOf(dataSnapshot.getChildrenCount()));
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {

			}
		});
	}*/

	private void getPostCount() {

		FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				int counter = 0;
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					Post post = snapshot.getValue(Post.class);

					if (post.getPublisher().equals(profileId))
						counter++;
				}

				posts.setText(String.valueOf(counter));
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	private void getFollowersAndFollowingCount() {

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId);

		ref.child("followers").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				followers.setText("" + dataSnapshot.getChildrenCount());
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		ref.child("following").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				following.setText("" + dataSnapshot.getChildrenCount());
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	private void userInfo() {
		FirebaseDatabase.getInstance().getReference().child("Users").child(profileId).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				User user = dataSnapshot.getValue(User.class);

				Picasso.get().load(user.getImageurl()).into(imageProfile);
				username.setText(user.getUsername());
				fullname.setText(user.getName());
				bio.setText(user.getBio());
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}
}

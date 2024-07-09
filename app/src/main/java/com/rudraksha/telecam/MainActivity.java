package com.rudraksha.telecam;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.rudraksha.telecam.Fragments.HomeFragment;
import com.rudraksha.telecam.Fragments.NotificationFragment;
import com.rudraksha.telecam.Fragments.ProfileFragment;
import com.rudraksha.telecam.Fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {

	private BottomNavigationView bottomNavigationView;
	private Fragment selectorFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		bottomNavigationView = findViewById(R.id.bottom_navigation);

		bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				int itemId = item.getItemId();
				if (itemId == R.id.nav_home)
					selectorFragment = new HomeFragment();
				else if (itemId == R.id.nav_search)
					selectorFragment = new SearchFragment();
				else if (itemId == R.id.nav_add) {
					selectorFragment = null;
					startActivity(new Intent(MainActivity.this, PostActivity.class));
				} else if (itemId == R.id.nav_heart)
					selectorFragment = new NotificationFragment();
				else if (itemId == R.id.nav_profile)
					selectorFragment = new ProfileFragment();

				if (selectorFragment != null) {
					getSupportFragmentManager().beginTransaction()
							.replace(R.id.fragment_container, selectorFragment).commit();
				}

				return true;
			}
		});

		Bundle intent = getIntent().getExtras();
		if (intent != null) {
			String profileId = intent.getString("publisherId");

			getSharedPreferences("PROFILE", MODE_PRIVATE).edit().putString("profileId",
					profileId).apply();

			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
					new ProfileFragment()).commit();
			bottomNavigationView.setSelectedItemId(R.id.nav_profile);
		} else {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
					new HomeFragment()).commit();
		}
	}
}

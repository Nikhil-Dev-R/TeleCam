package com.rudraksha.telecam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.rudraksha.telecam.Fragments.ProfileFragment;

public class OptionsActivity extends AppCompatActivity {

	private LinearLayout settings;
	private LinearLayout logOut;
	private LinearLayout help;
	private LinearLayout feedback;
	private ImageView back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);

		settings = findViewById(R.id.settings);
		logOut = findViewById(R.id.logout);
		help = findViewById(R.id.help);
		feedback = findViewById(R.id.feedback);

		back = findViewById(R.id.back);

		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(OptionsActivity.this, ProfileFragment.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
			}
		});

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("Options");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//startActivity(new Intent(OptionsActivity.this, Settings.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
			}
		});

		logOut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FirebaseAuth.getInstance().signOut();
				startActivity(new Intent(OptionsActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
				finish();
			}
		});

		help.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

		feedback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
	}
}

package com.rudraksha.telecam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {
	private ImageView iconImage;
	private LinearLayout linearLayout;
	private Button register;
	private Button login;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		iconImage = findViewById(R.id.icon);
		linearLayout = findViewById(R.id.linear_layout);
		register = findViewById(R.id.register);
		login = findViewById(R.id.login);

		linearLayout.animate().alpha(0f).setDuration(1);

		TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -1000);
		translateAnimation.setDuration(1000);
		translateAnimation.setFillAfter(false);
		translateAnimation.setAnimationListener(new MyAnimationListener());
		iconImage.startAnimation(translateAnimation);

		register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(StartActivity.this, RegisterActivity.class)
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});

		login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(StartActivity.this, LoginActivity.class)
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			startActivity(new Intent(StartActivity.this, MainActivity.class));
			finish();
		}
	}

	private class MyAnimationListener implements Animation.AnimationListener {

		@Override
		public void onAnimationStart(Animation animation) {

		}

		/**
		 * <p>Notifies the end of the animation. This callback is not invoked
		 * for animations with repeat count set to INFINITE.</p>
		 *
		 * @param animation The animation which reached its end.
		 */
		@Override
		public void onAnimationEnd(Animation animation) {
			iconImage.clearAnimation();
			iconImage.setVisibility(View.INVISIBLE);
			linearLayout.animate().alpha(1f).setDuration(1000);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}
	}

}
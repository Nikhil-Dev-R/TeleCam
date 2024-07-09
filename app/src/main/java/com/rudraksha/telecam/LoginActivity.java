package com.rudraksha.telecam;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

	private EditText emailEditText;
	private EditText passwordEditText;
	private Button loginUserButton;
	private TextView registerTextView;
	private DatabaseReference databaseReference;
	private FirebaseAuth firebaseAuth;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_login);

		emailEditText = findViewById(R.id.user_email);
		passwordEditText = findViewById(R.id.user_password);
		loginUserButton = findViewById(R.id.login_button);
		registerTextView = findViewById(R.id.register_user_text);

		databaseReference = FirebaseDatabase.getInstance().getReference();
		firebaseAuth = FirebaseAuth.getInstance();
		progressDialog = new ProgressDialog(this);

		registerTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
			}
		});

		loginUserButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String textEmail = emailEditText.getText().toString().trim();
				String textPassword = passwordEditText.getText().toString().trim();

				if (textEmail.isEmpty()) {
					emailEditText.setError("Email is required");
					emailEditText.requestFocus();
					return;
				}
				if (textPassword.isEmpty()) {
					passwordEditText.setError("Password is required");
					passwordEditText.requestFocus();
					return;
				}
				// Login the user
				loginUser(textEmail, textPassword);
			}
		});
	}

	private void loginUser(String textEmail, String textPassword) {
		progressDialog.setMessage("Logging in...");
		progressDialog.show();

		firebaseAuth.signInWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					progressDialog.dismiss();
					Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(LoginActivity.this, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				} else {
					progressDialog.dismiss();
					Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
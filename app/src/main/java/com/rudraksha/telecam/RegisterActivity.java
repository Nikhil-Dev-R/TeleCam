package com.rudraksha.telecam;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

	private EditText userNameEditText;
	private EditText fullNameEditText;
	private EditText emailEditText;
	private EditText passwordEditText;
	private EditText confirmPasswordEditText;
	private Button registerButton;
	private TextView loginUserTextView;
	private DatabaseReference databaseReference;
	private FirebaseAuth firebaseAuth;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		userNameEditText = findViewById(R.id.user_name);
		fullNameEditText = findViewById(R.id.full_name);
		emailEditText = findViewById(R.id.user_email);
		passwordEditText = findViewById(R.id.user_password);
		confirmPasswordEditText = findViewById(R.id.user_confirm_password);
		registerButton = findViewById(R.id.register_button);
		loginUserTextView = findViewById(R.id.login_text);

		databaseReference = FirebaseDatabase.getInstance().getReference();
		firebaseAuth = FirebaseAuth.getInstance();
		progressDialog = new ProgressDialog(this);

		loginUserTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
			}
		});

		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String textUserName = userNameEditText.getText().toString().trim();
				String textFullName = fullNameEditText.getText().toString().trim();
				String textEmail = emailEditText.getText().toString().trim();
				String textPassword = passwordEditText.getText().toString().trim();
				String textConfirmPassword = confirmPasswordEditText.getText().toString().trim();

				if (textUserName.isEmpty()) {
					userNameEditText.setError("Username is required");
					userNameEditText.requestFocus();
					return;
				}
				if (textFullName.isEmpty()) {
					fullNameEditText.setError("Full name is required");
					fullNameEditText.requestFocus();
					return;
				}
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
				if (textConfirmPassword.isEmpty()) {
					confirmPasswordEditText.setError("Confirm password is required");
					confirmPasswordEditText.requestFocus();
					return;
				}
				if (!textPassword.equals(textConfirmPassword)) {
					confirmPasswordEditText.setError("Password does not match");
					confirmPasswordEditText.requestFocus();
					return;
				}
				if (textPassword.length() < 8) {
					passwordEditText.setError("Password must be of at least 8 characters");
					passwordEditText.requestFocus();
					return;
				}
				// Define the pattern to match letters, numbers, and special characters
				/*String regex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher((CharSequence) passwordEditText);

				// Return if the password matches the pattern
				if(!matcher.matches()) {
					passwordEditText.setError("Password must contain alphabets, numerics and special characters");
					passwordEditText.requestFocus();
					return;
				}*/

				// Register the user
				registerUser(textUserName, textFullName, textEmail, textPassword);
			}
		});
	}

	private void registerUser(String userName, String fullName, String email, String password) {
		progressDialog.setMessage("Registering user...");
		progressDialog.show();

		// Create a new user with a unique ID
		firebaseAuth.createUserWithEmailAndPassword(email, password)
				.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
					@Override
					public void onSuccess(AuthResult authResult) {
						HashMap<String, Object> userMap = new HashMap<>();
						userMap.put("username", userName);
						userMap.put("fullname", fullName);
						userMap.put("email", email);
						userMap.put("password", password);
						userMap.put("id", firebaseAuth.getCurrentUser().getUid());
						userMap.put("bio", "");
						userMap.put("imageurl", "default");

						databaseReference.child("users").child(firebaseAuth.getCurrentUser()
								.getUid()).setValue(userMap).addOnCompleteListener(
								new OnCompleteListener<Void>() {
									@Override
									public void onComplete(@NonNull Task<Void> task) {
										if (task.isSuccessful()) {
											progressDialog.dismiss();
											Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
											Toast.makeText(RegisterActivity.this, "Update the profile for better experience", Toast.LENGTH_SHORT).show();
											startActivity(new Intent(RegisterActivity.this, MainActivity.class).addFlags(
													Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
											finish();
										} else {
											progressDialog.dismiss();
											Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
										}
									}
								}
						);
					}
				}).addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						progressDialog.dismiss();
						Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				});
	}
}
package com.example.rougelikegame.android.screens.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.adapters.ImageSourceAdapter;
import com.example.rougelikegame.android.models.meta.ImageSourceOption;
import com.example.rougelikegame.android.models.meta.User;
import com.example.rougelikegame.android.screens.auth.LandingActivity;
import com.example.rougelikegame.android.services.DatabaseService;
import com.example.rougelikegame.android.utils.ImageUtil;
import com.example.rougelikegame.android.utils.SharedPreferencesUtil;
import com.example.rougelikegame.android.utils.Validator;

import android.net.Uri;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class UpdateUserActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserProfileActivity";

    private EditText etUserFirstName, etUserLastName, etUserEmail, etUserPhone, etUserPassword;
    private TextView tvUserDisplayName, tvUserDisplayEmail;
    private Button btnUpdateProfile, btnSignOut;
    private View adminBadge;
    String selectedUid;
    User selectedUser;
    boolean isCurrentUser = false;
    DatabaseService databaseService;

    private ImageView ivProfileIcon;

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService = DatabaseService.getInstance();

        selectedUid = getIntent().getStringExtra("USER_UID");
        User currentUser = SharedPreferencesUtil.getUser(this);
        assert currentUser != null;

        if (selectedUid == null) {
            selectedUid = currentUser.getUid();
        }
        isCurrentUser = selectedUid.equals(currentUser.getUid());
        if (!currentUser.isAdmin() && !isCurrentUser) {
            // If the user is not an admin and the selected user is not the current user
            // then finish the activity
            Toast.makeText(this, "You are not authorized to view this profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Selected user: " + selectedUid);

        // Initialize the EditText fields
        etUserFirstName = findViewById(R.id.et_user_first_name);
        etUserLastName = findViewById(R.id.et_user_last_name);
        etUserEmail = findViewById(R.id.et_user_email);
        etUserPhone = findViewById(R.id.et_user_phone);
        etUserPassword = findViewById(R.id.et_user_password);
        tvUserDisplayName = findViewById(R.id.tv_user_display_name);
        tvUserDisplayEmail = findViewById(R.id.tv_user_display_email);
        btnUpdateProfile = findViewById(R.id.btn_edit_profile);
        btnSignOut = findViewById(R.id.btn_sign_out);
        adminBadge = findViewById(R.id.admin_badge);

        ivProfileIcon = findViewById(R.id.iv_profile_icon);

        ivProfileIcon.setOnClickListener(v -> {
            if (isCurrentUser) {
                showImageSourceDialog();
            } else {
                Toast.makeText(this, "You can only change your own photo", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdateProfile.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);

        // if the user is not the current user, hide the sign out button
        if (!isCurrentUser) {
            btnSignOut.setVisibility(View.GONE);
        }

        showUserProfile();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_edit_profile) {
            updateUserProfile();
            return;
        }
        if(v.getId() == R.id.btn_sign_out) {
            signOut();
        }
    }

    private void showUserProfile() {
        // Get the user data from database
        databaseService.getUser(selectedUid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                selectedUser = user;
                // Set the user data to the EditText fields
                etUserFirstName.setText(user.getFirstName());
                etUserLastName.setText(user.getLastName());
                etUserEmail.setText(user.getEmail());
                etUserPhone.setText(user.getPhone());
                etUserPassword.setText(user.getPassword());

                if (user.getProfileImage() != null) {
                    Bitmap bitmap = ImageUtil.convertFrom64base(user.getProfileImage());
                    if (bitmap != null) {
                        ivProfileIcon.setImageBitmap(bitmap);
                    }
                }

                // Update display fields
                String displayName = user.getFirstName() + " " + user.getLastName();
                tvUserDisplayName.setText(displayName);
                tvUserDisplayEmail.setText(user.getEmail());

                // Show/hide admin badge based on user's admin status
                if (user.isAdmin()) {
                    adminBadge.setVisibility(View.VISIBLE);
                    Log.d(TAG, "User is admin, showing admin badge");
                } else {
                    adminBadge.setVisibility(View.GONE);
                    Log.d(TAG, "User is not admin, hiding admin badge");
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Error getting user profile", e);
            }
        });

        // disable the EditText fields if the user is not the current user
        if (!isCurrentUser) {
            etUserEmail.setEnabled(false);
            etUserPassword.setEnabled(false);
        } else {
            etUserEmail.setEnabled(true);
            etUserPassword.setEnabled(true);
            btnUpdateProfile.setVisibility(View.VISIBLE);
        }
    }

    private void updateUserProfile() {
        if (selectedUser == null) {
            Log.e(TAG, "User not found");
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the updated user data from the EditText fields
        String firstName = etUserFirstName.getText().toString();
        String lastName = etUserLastName.getText().toString();
        String phone = etUserPhone.getText().toString();
        String email = etUserEmail.getText().toString();
        String password = etUserPassword.getText().toString();

        if (!isValid(firstName, lastName, phone, email, password)) {
            Log.e(TAG, "Invalid input");
            return;
        }

        // Update the user object
        selectedUser.setFirstName(firstName);
        selectedUser.setLastName(lastName);
        selectedUser.setPhone(phone);
        selectedUser.setEmail(email);
        selectedUser.setPassword(password);

        String base64Image = ImageUtil.convertTo64Base(ivProfileIcon);
        selectedUser.setProfileImage(base64Image);

        Log.d(TAG, "Profile image length = " +
            (base64Image == null ? "null" : base64Image.length()));

        // Update the user data in the authentication
        Log.d(TAG, "Updating user profile");
        Log.d(TAG, "Selected user UID: " + selectedUser.getUid());
        Log.d(TAG, "Is current user: " + isCurrentUser);
        Log.d(TAG, "User email: " + selectedUser.getEmail());
        Log.d(TAG, "User password: " + selectedUser.getPassword());



        if (!isCurrentUser && !selectedUser.isAdmin()) {
            Log.e(TAG, "Only the current user can update their profile");
            Toast.makeText(this, "You can only update your own profile", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (isCurrentUser) {
            updateUserInDatabase(selectedUser);
        }
        else if (selectedUser.isAdmin()) {
            // update the user in the database
            updateUserInDatabase(selectedUser);
        }
    }

    private void updateUserInDatabase(User user) {
        Log.d(TAG, "Updating user in database: " + user.getUid());
        databaseService.updateUser(user, false, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void result) {
                Log.d(TAG, "User profile updated successfully");
                Toast.makeText(UpdateUserActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                showUserProfile(); // Refresh the profile view
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Error updating user profile", e);
                Toast.makeText(UpdateUserActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValid(String firstName, String lastName, String phone, String email, String password) {
        if (!Validator.isNameValid(firstName)) {
            etUserFirstName.setError("First name is required");
            etUserFirstName.requestFocus();
            return false;
        }
        if (!Validator.isNameValid(lastName)) {
            etUserLastName.setError("Last name is required");
            etUserLastName.requestFocus();
            return false;
        }
        if (!Validator.isPhoneValid(phone)) {
            etUserPhone.setError("Phone number is required");
            etUserPhone.requestFocus();
            return false;
        }
        if (!Validator.isEmailValid(email)) {
            etUserEmail.setError("Email is required");
            etUserEmail.requestFocus();
            return false;
        }
        if (!Validator.isPasswordValid(password)) {
            etUserPassword.setError("Password is required");
            etUserPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void showImageSourceDialog() {
        List<ImageSourceOption> options = new ArrayList<>();

        options.add(new ImageSourceOption(
            "Camera",
            "Take a photo",
            R.drawable.photo_camera
        ));

        options.add(new ImageSourceOption(
            "Gallery",
            "Choose from gallery",
            R.drawable.gallery_thumbnail
        ));

        ImageSourceAdapter adapter = new ImageSourceAdapter(
            this,
            options,
            option -> {
                if ("Camera".equals(option.getTitle())) {
                    ImageUtil.requestPermission(this);
                    openCamera();
                } else {
                    openGallery();
                }
            }
        );

        new AlertDialog.Builder(this)
            .setAdapter(adapter, null)
            .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        try {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                ivProfileIcon.setImageBitmap(bitmap);
            }

            if (requestCode == REQUEST_GALLERY) {
                Uri imageUri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ivProfileIcon.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load image", e);
        }
    }

    private void signOut() {
        Log.d(TAG, "Sign out button clicked");
        SharedPreferencesUtil.signOutUser(UpdateUserActivity.this);

        Log.d(TAG, "User signed out, redirecting to LandingActivity");
        Intent landingIntent = new Intent(UpdateUserActivity.this, LandingActivity.class);
        landingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(landingIntent);
    }
}

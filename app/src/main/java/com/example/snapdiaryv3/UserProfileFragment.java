package com.example.snapdiaryv3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UserProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextView emailText;
    private Button logoutButton;
    private ImageView profileImageView;
    private Button uploadImageButton;
    private Button changePasswordButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        emailText = view.findViewById(R.id.emailText);
        logoutButton = view.findViewById(R.id.logoutButton);
        profileImageView = view.findViewById(R.id.profileImageView);
        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);

        if (user != null) {
            emailText.setText(user.getEmail());
            // Load profile image if available
            // You can use Glide or Picasso to load the image from the URL
            // Example:
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                        .error(R.drawable.ic_arrow_back) // Error image if loading fails
                        .into(profileImageView);
            } else {
                // Handle case where user's photo URL is null
                // You can set a default image or hide the ImageView
                profileImageView.setImageResource(R.drawable.ic_add);
            }
        }

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            getActivity().finish();
            startActivity(new Intent(getActivity(), MainActivity.class));
        });

        uploadImageButton.setOnClickListener(v -> {
            // Implement image upload functionality
            // Example: Start an intent to pick an image from gallery or camera
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        changePasswordButton.setOnClickListener(v -> {
            // Implement password change functionality
            // Example: Start an activity or dialog to input new password
            // and use Firebase Authentication to update the password
            showChangePasswordDialog();
        });

        return view;
    }

    private void showChangePasswordDialog() {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_change_password, null);

        // Find views in the dialog layout
        EditText editTextCurrentPassword = dialogView.findViewById(R.id.editTextCurrentPassword);
        EditText editTextNewPassword = dialogView.findViewById(R.id.editTextNewPassword);
        EditText editTextConfirmNewPassword = dialogView.findViewById(R.id.editTextConfirmNewPassword);

        // Build AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setTitle("Change Password")
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get input values
                        String currentPassword = editTextCurrentPassword.getText().toString().trim();
                        String newPassword = editTextNewPassword.getText().toString().trim();
                        String confirmNewPassword = editTextConfirmNewPassword.getText().toString().trim();

                        // Validate input
                        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!newPassword.equals(confirmNewPassword)) {
                            Toast.makeText(getActivity(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Change password using Firebase Authentication
                        reauthenticateUser(currentPassword, newPassword);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reauthenticateUser(String currentPassword, final String newPassword) {
        // Re-authenticate user with current password
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Password re-authentication successful, proceed with password update
                            updateUserPassword(newPassword);
                        } else {
                            // Re-authentication failed
                            Toast.makeText(getActivity(), "Re-authentication failed. Please check current password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserPassword(String newPassword) {
        // Update user's password in Firebase Authentication
        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            // Now you can upload this filePath to Firebase Storage or update user's profile image
            // Example:
            // uploadToFirebaseStorage(filePath);

            // Load the selected image into profileImageView using Glide
            Glide.with(this)
                    .load(filePath)
                    .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                    .error(R.drawable.ic_arrow_back) // Error image if loading fails
                    .into(profileImageView);

            // Update Firebase user's profile with the new photo URL if necessary
            if (user != null) {
                user.updateProfile(new UserProfileChangeRequest.Builder()
                                .setPhotoUri(filePath)
                                .build())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Profile image updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Failed to update profile image", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

}

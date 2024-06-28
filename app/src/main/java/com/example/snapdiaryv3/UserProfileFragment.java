package com.example.snapdiaryv3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
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
            Toast.makeText(getActivity(), "Change password functionality to be implemented", Toast.LENGTH_SHORT).show();
        });

        return view;
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

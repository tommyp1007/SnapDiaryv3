package com.example.snapdiaryv3;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class CreateDiaryFragment extends Fragment {

    private static final String TAG = "CreateDiaryFragment";

    private EditText editTextDescription;
    private Button buttonCamera, buttonSelectImage, buttonRecordAudio, buttonStopAudio, buttonPlaybackAudio, buttonSave;
    private RatingBar ratingBarMood;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private DatabaseReference diaryRef;
    private FirebaseAuth mAuth;

    // For audio recording and playback
    private MediaRecorder mediaRecorder;
    private String audioFilePath = null;
    private MediaPlayer mediaPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_diary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        String userId = mAuth.getCurrentUser().getUid();
        diaryRef = databaseReference.child("diaries").child(userId);

        editTextDescription = view.findViewById(R.id.editTextDescription);
        buttonCamera = view.findViewById(R.id.buttonCamera);
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage);
        buttonRecordAudio = view.findViewById(R.id.buttonRecordAudio);
        buttonStopAudio = view.findViewById(R.id.buttonStopAudio);
        buttonPlaybackAudio = view.findViewById(R.id.buttonPlaybackAudio);
        buttonSave = view.findViewById(R.id.buttonSave);
        ratingBarMood = view.findViewById(R.id.ratingBarMood);

        buttonCamera.setOnClickListener(v -> dispatchTakePictureIntent());
        buttonSelectImage.setOnClickListener(v -> showImageSourceDialog());
        buttonRecordAudio.setOnClickListener(v -> checkAudioPermissionsAndStartRecording());
        buttonStopAudio.setOnClickListener(v -> stopRecording());
        buttonPlaybackAudio.setOnClickListener(v -> playRecording());
        buttonSave.setOnClickListener(v -> saveDiary());
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Image")
                .setItems(new String[]{"Take Picture", "Choose from Gallery"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Take Picture
                            dispatchTakePictureIntent();
                            break;
                        case 1: // Choose from Gallery
                            dispatchPickPictureIntent();
                            break;
                    }
                })
                .show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchPickPictureIntent() {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (pickPictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(pickPictureIntent, REQUEST_IMAGE_PICK);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (data != null && data.getExtras() != null) {
                    imageUri = data.getData();
                    Toast.makeText(requireContext(), "Image captured!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    Toast.makeText(requireContext(), "Image selected!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void checkAudioPermissionsAndStartRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                startRecording();
            }
        } else {
            startRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecording() {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User is not authenticated");
            Toast.makeText(requireContext(), "You need to be logged in to record audio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaRecorder == null) {
            // Initialize media recorder
            String fileName = "audio_" + System.currentTimeMillis() + ".3gp";
            File storageDir = requireActivity().getExternalFilesDir("audio");
            if (storageDir != null) {
                audioFilePath = storageDir.getAbsolutePath() + "/" + fileName;
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setOutputFile(audioFilePath);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                try {
                    mediaRecorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaRecorder.start();
                Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show();

                // Update UI
                buttonRecordAudio.setVisibility(View.GONE);
                buttonStopAudio.setVisibility(View.VISIBLE);
                buttonPlaybackAudio.setVisibility(View.GONE);
            }
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_SHORT).show();

            // Update UI
            buttonRecordAudio.setVisibility(View.GONE);
            buttonStopAudio.setVisibility(View.GONE);
            buttonPlaybackAudio.setVisibility(View.VISIBLE);
        }
    }

    private void playRecording() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(requireContext(), "Playing audio", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDiary() {
        String description = editTextDescription.getText().toString().trim();
        float moodLevel = ratingBarMood.getRating();

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String entryId = diaryRef.push().getKey();
        if (entryId == null) {
            Toast.makeText(requireContext(), "Failed to generate diary entry key", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();

        DiaryEntry diaryEntry = new DiaryEntry(description, moodLevel, imageUri != null ? imageUri.toString() : null, audioFilePath, timestamp);

        diaryRef.child(entryId).setValue(diaryEntry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Diary entry saved successfully", Toast.LENGTH_SHORT).show();
                    editTextDescription.setText("");
                    ratingBarMood.setRating(0);
                    imageUri = null;
                    audioFilePath = null;

                    // Reset UI after saving
                    buttonRecordAudio.setVisibility(View.VISIBLE);
                    buttonStopAudio.setVisibility(View.GONE);
                    buttonPlaybackAudio.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to save diary entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

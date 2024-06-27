package com.example.snapdiaryv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiaryEntryDetailsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference diaryRef;
    private String userId;

    private TextView textViewDescription;
    private RatingBar ratingBarMood;
    private ImageView imageViewPhoto;
    private TextView textViewTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_diary_entry_details);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            // User is not logged in, redirect to login activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = mAuth.getCurrentUser().getUid();
        diaryRef = FirebaseDatabase.getInstance().getReference("diaries").child(userId);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this::onBackButtonClick);

        textViewDescription = findViewById(R.id.textViewDescription);
        ratingBarMood = findViewById(R.id.ratingBarMood);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        textViewTimestamp = findViewById(R.id.textViewTimestamp);

        String entryId = getIntent().getStringExtra("entryId");

        // Fetch entry details from database using entryId and display in UI
        fetchEntryDetails(entryId);
    }

    public void onBackButtonClick(View view) {
        onBackPressed();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchEntryDetails(String entryId) {
        diaryRef.child(entryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DiaryEntry entry = snapshot.getValue(DiaryEntry.class);
                if (entry != null) {
                    textViewDescription.setText(entry.getDescription());
                    ratingBarMood.setRating(entry.getMoodLevel());
                    if (entry.getImageUri() != null) {
                        Glide.with(DiaryEntryDetailsActivity.this)
                                .load(entry.getImageUri())
                                .into(imageViewPhoto);
                    } else {
                        imageViewPhoto.setVisibility(View.GONE);
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String dateString = sdf.format(new Date(entry.getTimestamp()));
                    textViewTimestamp.setText(dateString);
                } else {
                    Toast.makeText(DiaryEntryDetailsActivity.this, "Entry not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DiaryEntryDetailsActivity.this, "Failed to load entry details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.example.snapdiaryv3;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements DiaryAdapter.OnEntryClickListener {

    private RecyclerView recyclerView;
    private DiaryAdapter diaryAdapter;

    private DatabaseReference diaryRef;
    private FirebaseAuth mAuth;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_diaries, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        diaryRef = database.getReference("diaries").child(userId);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        diaryAdapter = new DiaryAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(diaryAdapter);

        // Set click listener
        diaryAdapter.setOnEntryClickListener(this);

        // Load latest three diary entries
        loadLatestThreeDiaryEntries();

        return view;
    }

    private void loadLatestThreeDiaryEntries() {
        // Query to fetch latest three diary entries sorted by timestamp in descending order
        diaryRef.orderByChild("timestamp").limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<DiaryEntry> entries = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DiaryEntry entry = dataSnapshot.getValue(DiaryEntry.class);
                    if (entry != null) {
                        entry.setEntryId(dataSnapshot.getKey()); // Set the entryId from Firebase key
                        entries.add(entry);
                    }
                }
                // Reverse the list to maintain descending order by timestamp
                Collections.reverse(entries);
                diaryAdapter.setDiaryEntries(entries);
                diaryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load diary entries: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEntryClicked(DiaryEntry entry) {
        // Handle item click here (optional)
    }

    @Override
    public void onEntryDetailsClicked(DiaryEntry entry) {
        Intent intent = new Intent(requireContext(), DiaryEntryDetailsActivity.class);
        intent.putExtra("entryId", entry.getEntryId());
        startActivity(intent);
    }

    @Override
    public void onEntryDeleteClicked(int position) {
        if (mAuth.getCurrentUser() != null) {
            List<DiaryEntry> entries = diaryAdapter.getDiaryEntries();
            if (entries != null && position >= 0 && position < entries.size()) {
                DiaryEntry entryToDelete = entries.get(position);
                String entryId = entryToDelete.getEntryId();

                diaryRef.child(entryId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), "Diary entry deleted", Toast.LENGTH_SHORT).show();
                            // Optionally, refresh your diary entries here if needed
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Failed to delete diary entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(requireContext(), "Invalid entry position", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }


}

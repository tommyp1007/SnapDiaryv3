package com.example.snapdiaryv3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavedDiariesFragment extends Fragment {

    private RecyclerView recyclerView;
    private DiaryAdapter diaryAdapter;

    private DatabaseReference diaryRef;
    private FirebaseAuth mAuth;

    public SavedDiariesFragment() {
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

        // Load diary entries
        loadDiaryEntries();

        return view;
    }

    private void loadDiaryEntries() {
        diaryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<DiaryEntry> entries = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DiaryEntry entry = dataSnapshot.getValue(DiaryEntry.class);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
                diaryAdapter.setDiaryEntries(entries);
                diaryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load diary entries: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }
}

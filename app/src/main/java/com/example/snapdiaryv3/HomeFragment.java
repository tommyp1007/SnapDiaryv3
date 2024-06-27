package com.example.snapdiaryv3;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private DiaryAdapter diaryAdapter;
    private DatabaseReference diaryRef;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase components
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Handle the case where user is not logged in
            // Redirect or show login screen
            return view;
        }

        String userId = currentUser.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        diaryRef = database.getReference("diaries").child(userId);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        diaryAdapter = new DiaryAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(diaryAdapter);

        // Load latest 3 diary entries
        loadLatestDiaryEntries();

        return view;
    }

    private void loadLatestDiaryEntries() {
        diaryRef.orderByChild("timestamp").limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<DiaryEntry> entries = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DiaryEntry entry = dataSnapshot.getValue(DiaryEntry.class);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
                // Reverse the list to display latest entries first
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
}

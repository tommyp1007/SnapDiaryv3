package com.example.snapdiaryv3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MoodTrackerFragment extends Fragment {

    private RecyclerView recyclerView;
    private DiaryAdapter diaryAdapter;
    private DatabaseReference diaryRef;
    private FirebaseAuth mAuth;


    private List<DiaryEntry> diaryEntries;
    private List<String> notifications;
    private TextView notificationTextView;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String description;
    private float moodLevel;
    private String imageUri;
    private String audioUri;
    private long timestamp; // Timestamp in milliseconds
    private String mParam1;
    private String mParam2;

    public MoodTrackerFragment() {
        // Required empty public constructor
    }

    public static MoodTrackerFragment newInstance(String param1, String param2) {
        MoodTrackerFragment fragment = new MoodTrackerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Initialize your data lists
        diaryEntries = new ArrayList<>();
        notifications = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_tracker, container, false);
        notificationTextView = view.findViewById(R.id.notificationTextView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDiaryEntries();
    }

    private void loadDiaryEntries() {
        // Mock data loading, replace this with actual data fetching logic
        diaryEntries = getDiaryEntriesFromAdapter();

        float averageMood = calculateAverageMood();
        displayNotification(averageMood);
    }

    private List<DiaryEntry> getDiaryEntriesFromAdapter() {
        // Fetch entries from your DiaryAdapter
        List<DiaryEntry> entries = new ArrayList<>();

        // Example: Retrieve entries from the adapter (assuming diaryAdapter is initialized)
        // Replace with actual method to get entries from your adapter
        if (diaryAdapter != null) {
            entries = diaryAdapter.getDiaryEntries();
        }

        // Mock data for demonstration, using the correct constructor
        entries.add(new DiaryEntry("Entry 1", 3.0f, null, null, System.currentTimeMillis(), null));
        entries.add(new DiaryEntry("Entry 2", 4.5f, null, null, System.currentTimeMillis(), null));
        entries.add(new DiaryEntry("Entry 3", 1.0f, null, null, System.currentTimeMillis(), null));

        return entries;
    }


    private float calculateAverageMood() {
        float totalMood = 0;
        for (DiaryEntry entry : diaryEntries) {
            totalMood += entry.getMoodLevel();
        }
        return totalMood / diaryEntries.size();
    }

    private void displayNotification(float averageMood) {
        String notificationMessage = generateNotificationMessage(averageMood);
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        String notificationEntry = timestamp + " - " + notificationMessage;
        notifications.add(notificationEntry);

        // Display the latest notification
        notificationTextView.setText(notificationEntry);

        // Save notifications persistently if needed (e.g., using SharedPreferences, database, etc.)
    }

    private String generateNotificationMessage(float averageMood) {
        Random random = new Random();
        String[] encouragementMessages = {
                "Keep going! You got this!",
                "Every day is a fresh start.",
                "You're stronger than you think."
        };
        String[] congratulatoryMessages = {
                "You're doing great!",
                "Keep up the awesome work!",
                "You're on fire!"
        };

        if (averageMood < 2.0) {
            return encouragementMessages[random.nextInt(encouragementMessages.length)];
        } else if (averageMood > 4.0) {
            return congratulatoryMessages[random.nextInt(congratulatoryMessages.length)];
        } else {
            return "Keep tracking your mood!";
        }
    }
}

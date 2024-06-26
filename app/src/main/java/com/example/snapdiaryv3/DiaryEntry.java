package com.example.snapdiaryv3;

public class DiaryEntry {
    private String description;
    private float moodLevel;
    private String imageUri;
    private String audioUri;
    private long timestamp; // Timestamp in milliseconds

    private String entryId;

    // Default constructor required for calls to DataSnapshot.getValue(DiaryEntry.class)
    public DiaryEntry() {
    }

    // Parameterized constructor
    public DiaryEntry(String description, float moodLevel, String imageUri, String audioUri, long timestamp ) {
        this.description = description;
        this.moodLevel = moodLevel;
        this.imageUri = imageUri;
        this.audioUri = audioUri;
        this.timestamp = timestamp;


    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getMoodLevel() {
        return moodLevel;
    }

    public void setMoodLevel(float moodLevel) {
        this.moodLevel = moodLevel;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getAudioUri() {
        return audioUri;
    }

    public void setAudioUri(String audioUri) {
        this.audioUri = audioUri;
    }

    public String getEntryId() {
        return entryId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

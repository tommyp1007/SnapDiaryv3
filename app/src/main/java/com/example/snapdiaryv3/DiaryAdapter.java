package com.example.snapdiaryv3;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    private Context context;
    private List<DiaryEntry> diaryEntries;

    public DiaryAdapter(Context context, List<DiaryEntry> diaryEntries) {
        this.context = context;
        this.diaryEntries = diaryEntries;
    }

    public void setDiaryEntries(List<DiaryEntry> diaryEntries) {
        this.diaryEntries = diaryEntries;
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diary_entry, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntry entry = diaryEntries.get(position);
        holder.textViewDescription.setText(entry.getDescription());
        holder.ratingBarMood.setRating(entry.getMoodLevel());

        // Display image if available
        if (entry.getImageUri() != null) {
            holder.imageViewPhoto.setVisibility(View.VISIBLE);
            Glide.with(context).load(entry.getImageUri()).into(holder.imageViewPhoto);
        } else {
            holder.imageViewPhoto.setVisibility(View.GONE);
        }

        // Display audio button if audioUri is available
        if (entry.getAudioUri() != null) {
            holder.buttonPlayAudio.setVisibility(View.VISIBLE);
            holder.buttonPlayAudio.setOnClickListener(v -> {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(context, Uri.parse(entry.getAudioUri()));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            holder.buttonPlayAudio.setVisibility(View.GONE);
        }
    }



    @Override
    public int getItemCount() {
        return diaryEntries != null ? diaryEntries.size() : 0;
    }

    public static class DiaryViewHolder extends RecyclerView.ViewHolder {

        TextView textViewDescription;
        RatingBar ratingBarMood;
        ImageView imageViewPhoto;
        Button buttonPlayAudio;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            ratingBarMood = itemView.findViewById(R.id.ratingBarMood);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
            buttonPlayAudio = itemView.findViewById(R.id.buttonPlayAudio);
        }
    }
}

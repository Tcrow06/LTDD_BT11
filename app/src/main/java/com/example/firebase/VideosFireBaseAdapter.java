package com.example.firebase;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class VideosFireBaseAdapter extends RecyclerView.Adapter<VideosFireBaseAdapter.VideoViewHolder> {

    private List<Video1Model> videoList;
    private Context context;

    private VideoViewHolder currentPlayingHolder;


    public VideosFireBaseAdapter(List<Video1Model> videoList) {
        this.videoList = videoList;
        Log.d("VideosFireBaseAdapter", "Adapter initialized with " + (videoList != null ? videoList.size() : 0) + " videos");
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.single_video_row, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {

        if (currentPlayingHolder != null && currentPlayingHolder != holder) {
            currentPlayingHolder.videoView.pause();
        }
        currentPlayingHolder = holder;
        Log.d("VideosFireBaseAdapter", "onBindViewHolder called for position: " + position);
        Video1Model video = videoList.get(position);

        holder.textVideoTitle.setText(video.getTitle());
        holder.textVideoDescription.setText(video.getDescription());
        holder.textLikes.setText(String.valueOf(video.getLikes()));
        holder.textDislikes.setText(String.valueOf(video.getDislikes()));

        String userId = video.getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.e("VideosFireBaseAdapter", "Invalid userId for video: " + video.getVideoId());
            holder.textUsername.setText("Unknown");
            holder.profileImageTopRight.setImageResource(R.drawable.ic_person_pin);
            holder.profileImageTopRight2.setImageResource(R.drawable.ic_person_pin);
            return;
        }

        Log.d("VideosFireBaseAdapter", "Querying userprofile for userId: " + userId);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("userprofile").child(userId);;
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("VideosFireBaseAdapter", "onDataChange called for userId: " + userId);
                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                if (userProfile != null) {
                    holder.textUsername.setText(userProfile.getEmail() != null ? userProfile.getEmail() : "Unknown");

                    String profileImageUrl = userProfile.getProfileImageUrl();
                    if (profileImageUrl == null || profileImageUrl.isEmpty()) {
                        profileImageUrl = "https://picsum.photos/200"; // Giá trị mặc định
                        Log.d("VideosFireBaseAdapter", "ProfileImageUrl is null, using default: " + profileImageUrl);
                    } else {
                        Log.d("VideosFireBaseAdapter", "ProfileImageUrl: " + profileImageUrl);
                    }

                    Log.d("VideosFireBaseAdapter", "Loading image with Glide: " + profileImageUrl);
                    Glide.with(context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_pin)
                            .error(R.drawable.ic_person_pin)
                            .circleCrop()
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("VideosFireBaseAdapter", "Glide load failed: " + (e != null ? e.getMessage() : "Unknown error"));
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Log.d("VideosFireBaseAdapter", "Glide load successful");
                                    return false;
                                }
                            })
                            .into(holder.profileImageTopRight);

                    Glide.with(context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_pin)
                            .error(R.drawable.ic_person_pin)
                            .circleCrop()
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("VideosFireBaseAdapter", "Glide load failed for profileImageTopRight2: " + (e != null ? e.getMessage() : "Unknown error"));
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Log.d("VideosFireBaseAdapter", "Glide load successful for profileImageTopRight2");
                                    return false;
                                }
                            })
                            .into(holder.profileImageTopRight2);
                } else {
                    Log.d("VideosFireBaseAdapter", "UserProfile is null for userId: " + userId);
                    holder.textUsername.setText("Unknown");
                    holder.profileImageTopRight.setImageResource(R.drawable.ic_person_pin);
                    holder.profileImageTopRight2.setImageResource(R.drawable.ic_person_pin);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("VideosFireBaseAdapter", "Failed to load user profile for userId " + userId + ": " + error.getMessage());
                holder.textUsername.setText("Unknown");
                holder.profileImageTopRight.setImageResource(R.drawable.ic_person_pin);
                holder.profileImageTopRight2.setImageResource(R.drawable.ic_person_pin);
            }
        });

        holder.profileImageTopRight.setOnClickListener(v -> {
            Log.d("VideosFireBaseAdapter", "Profile image clicked, opening ProfileReviewActivity for userId: " + userId);
            Intent intent = new Intent(context, ProfileReviewActivity.class);
            intent.putExtra("userId", video.getUserId());
            context.startActivity(intent);
        });

        holder.profileImageTopRight2.setOnClickListener(v -> {
            Log.d("VideosFireBaseAdapter", "Profile image 2 clicked, opening ProfileReviewActivity for userId: " + userId);
            Intent intent = new Intent(context, ProfileReviewActivity.class);
            intent.putExtra("userId", video.getUserId());
            context.startActivity(intent);
        });

        holder.videoView.setVideoURI(Uri.parse(video.getVideoUrl()));
        holder.videoView.setOnPreparedListener(mp -> {



            Log.d("VideosFireBaseAdapter", "Video prepared for position: " + position);
            holder.videoProgressBar.setVisibility(View.GONE);

            mp.start();
            mp.setLooping(true);

            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            float videoProportion = (float) videoWidth / (float) videoHeight;

            int screenWidth = holder.videoView.getWidth();
            int screenHeight = holder.videoView.getHeight();
            float screenProportion = (float) screenWidth / (float) screenHeight;

            ViewGroup.LayoutParams lp = holder.videoView.getLayoutParams();

            if (videoProportion > screenProportion) {
                lp.width = screenWidth;
                lp.height = (int) (screenWidth / videoProportion);
            } else {
                lp.height = screenHeight;
                lp.width = (int) (screenHeight * videoProportion);
            }

            holder.videoView.setLayoutParams(lp);
        });
        holder.videoView.setOnErrorListener((mp, what, extra) -> {
            Log.e("VideosFireBaseAdapter", "Video error at position: " + position + ", what: " + what + ", extra: " + extra);
            holder.videoProgressBar.setVisibility(View.GONE);
            return true;
        });

        holder.favorites.setOnClickListener(v -> {
            holder.favorites.setImageResource(R.drawable.ic_fill_favorite);
            Log.d("VideosFireBaseAdapter", "Like button clicked for video: " + video.getVideoId());
            long newLikes = video.getLikes() + 1;
            updateLikesDislikes(video.getVideoId(), newLikes, video.getDislikes());
            video.setLikes(newLikes);
            holder.textLikes.setText(String.valueOf(newLikes));
        });

        holder.dislikeButton.setOnClickListener(v -> {
            Log.d("VideosFireBaseAdapter", "Dislike button clicked for video: " + video.getVideoId());
            long newDislikes = video.getDislikes() + 1;
            updateLikesDislikes(video.getVideoId(), video.getLikes(), newDislikes);
            video.setDislikes(newDislikes);
            holder.textDislikes.setText(String.valueOf(newDislikes));
        });

        holder.videoView.setOnClickListener(v -> {
            if (holder.videoView.isPlaying()) {
                holder.videoView.pause();
            } else {
                holder.videoView.start();
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        holder.videoView.stopPlayback();
        if (holder == currentPlayingHolder) {
            currentPlayingHolder = null;
        }

        Log.d("VideosFireBaseAdapter", "Video playback stopped for recycled view");


    }

    @Override
    public int getItemCount() {
        int count = videoList != null ? videoList.size() : 0;
        Log.d("VideosFireBaseAdapter", "getItemCount: " + count);
        return count;
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        VideoView videoView;
        ProgressBar videoProgressBar;
        TextView textUsername, textVideoTitle, textVideoDescription, textLikes, textDislikes;
        ImageView profileImageTopRight, profileImageTopRight2, imPerson, favorites, dislikeButton, imShare, imMore;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            videoProgressBar = itemView.findViewById(R.id.videoProgressBar);
            textUsername = itemView.findViewById(R.id.textUsername);
            textVideoTitle = itemView.findViewById(R.id.textVideoTitle);
            textVideoDescription = itemView.findViewById(R.id.textVideoDescription);
            textLikes = itemView.findViewById(R.id.textLikes);
            textDislikes = itemView.findViewById(R.id.textDislikes);
            profileImageTopRight = itemView.findViewById(R.id.profileImageTopRight);
            profileImageTopRight2 = itemView.findViewById(R.id.profileImageTopRight2);
            imPerson = itemView.findViewById(R.id.imPerson);
            favorites = itemView.findViewById(R.id.favorites);
            dislikeButton = itemView.findViewById(R.id.dislikeButton);
            imShare = itemView.findViewById(R.id.imShare);
            imMore = itemView.findViewById(R.id.imMore);
        }
    }

    private void updateLikesDislikes(String videoId, long likes, long dislikes) {
        DatabaseReference videoRef = FirebaseDatabase.getInstance()
                .getReference("videos").child(videoId);

        videoRef.child("likes").setValue(likes);
        videoRef.child("dislikes").setValue(dislikes);
        Log.d("VideosFireBaseAdapter", "Updated likes: " + likes + ", dislikes: " + dislikes + " for video: " + videoId);
    }
}
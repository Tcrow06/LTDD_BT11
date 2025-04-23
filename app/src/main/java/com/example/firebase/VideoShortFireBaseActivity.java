package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class VideoShortFireBaseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideosFireBaseAdapter adapter;
    private List<Video1Model> videoList;
    private FirebaseAuth auth;

    VideosFireBaseAdapter.VideoViewHolder currentPlayingHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vs_firebase);

        // Kiểm tra đăng nhập
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e("VideoShortFireBaseActivity", "User is not logged in");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        Log.d("VideoShortFireBaseActivity", "User logged in with UID: " + auth.getCurrentUser().getUid());

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        videoList = new ArrayList<>();
        adapter = new VideosFireBaseAdapter(videoList);
        recyclerView.setAdapter(adapter);

        // Tải danh sách video
        loadVideos();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Cuộn xong rồi, lấy view đang nằm chính giữa
                    View snappedView = snapHelper.findSnapView(layoutManager);
                    if (snappedView != null) {
                        int position = layoutManager.getPosition(snappedView);
                        playVideoAtPosition(position);
                    }
                }
            }
        });



    }

    private void playVideoAtPosition(int position) {
        if (currentPlayingHolder != null) {
            currentPlayingHolder.videoView.pause();
        }

        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder instanceof VideosFireBaseAdapter.VideoViewHolder) {
            currentPlayingHolder = (VideosFireBaseAdapter.VideoViewHolder) viewHolder;
            currentPlayingHolder.videoView.start();
        }
    }

    private void loadVideos() {

        DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference("videos");
        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Video1Model video=null;
                    try {
                        video = dataSnapshot.getValue(Video1Model.class);
                    }catch (Exception e){
                        e.getMessage();
                    }
                    if (video != null) {
                        video.setVideoId(dataSnapshot.getKey());
                        videoList.add(video);
                    }
                }
                Log.d("VideoShortFireBaseActivity", "Loaded " + videoList.size() + " videos");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("VideoShortFireBaseActivity", "Failed to load videos: " + error.getMessage());
            }
        });
    }


}
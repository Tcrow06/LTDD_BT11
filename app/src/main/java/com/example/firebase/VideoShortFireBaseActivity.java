package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class VideoShortFireBaseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideosFireBaseAdapter adapter;
    private List<Video1Model> videoList;
    private FirebaseAuth auth;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        videoList = new ArrayList<>();
        adapter = new VideosFireBaseAdapter(videoList);
        recyclerView.setAdapter(adapter);

        // Tải danh sách video
        loadVideos();


    }

    private void loadVideos() {
        DatabaseReference videoRef = FirebaseDatabase.getInstance(
                        "https://video-fire-base-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("shortvideo");

        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Video1Model video = dataSnapshot.getValue(Video1Model.class);
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
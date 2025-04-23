package com.example.firebase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileReviewActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView tvEmail, tvVideoCount;
    private Button btnUpdateProfileImage;
    private String userId;
    private Uri imageUri;
    private static final int STORAGE_PERMISSION_CODE = 100;

    // ActivityResultLauncher để chọn ảnh
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    uploadImage();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_review);

        profileImage = findViewById(R.id.profileImage);
        tvEmail = findViewById(R.id.tvEmail);
        tvVideoCount = findViewById(R.id.tvVideoCount);
        btnUpdateProfileImage = findViewById(R.id.btnUpdateProfileImage);

        // Lấy userId từ Intent
        userId = getIntent().getStringExtra("userId");

        // Lấy thông tin người dùng và số lượng video
        fetchUserProfile();
        fetchVideoCount();

        // Sự kiện nhấn nút cập nhật ảnh
        btnUpdateProfileImage.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                pickImage();
            } else {
                requestStoragePermission();
            }
        });
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền truy cập bộ nhớ!", Toast.LENGTH_LONG).show();
                pickImage();
            } else {
                Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void pickImage() {
        pickImageLauncher.launch("image/*");
    }

    private void uploadImage() {
        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh trước khi upload!", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Đang upload ảnh...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(imageUri)
                .unsigned("my_unsigned_preset")
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(ProfileReviewActivity.this, "Bắt đầu upload ảnh...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("url").toString();
                        Toast.makeText(ProfileReviewActivity.this, "Upload ảnh thành công!", Toast.LENGTH_LONG).show();

                        Log.d("UploadImage", "Image URL: " + imageUrl);

                        // Lưu URL ảnh vào Realtime Database
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("userprofile").child(userId);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("profileImageUrl", imageUrl);

                        userRef.updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("UploadImage", "Successfully saved image URL to database");
                                    Toast.makeText(ProfileReviewActivity.this,
                                            "Đã lưu URL ảnh vào database thành công!",
                                            Toast.LENGTH_LONG).show();

                                    // Cập nhật ảnh trên giao diện
                                    Glide.with(ProfileReviewActivity.this)
                                            .load(imageUrl)
                                            .placeholder(R.drawable.ic_person_pin)
                                            .error(R.drawable.ic_person_pin)
                                            .circleCrop()
                                            .into(profileImage);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UploadImage", "Failed to save image URL to database: " + e.getMessage());
                                    Toast.makeText(ProfileReviewActivity.this,
                                            "Lỗi lưu URL ảnh vào database: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(ProfileReviewActivity.this,
                                "Lỗi upload ảnh: " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Toast.makeText(ProfileReviewActivity.this,
                                "Đang thử lại upload ảnh...",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .dispatch();
    }

    private void fetchUserProfile() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("userprofile").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                if (userProfile != null) {
                    tvEmail.setText(userProfile.getEmail() != null ? userProfile.getEmail() : "Unknown");
                    if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty()) {
                        Glide.with(ProfileReviewActivity.this)
                                .load(userProfile.getProfileImageUrl())
                                .placeholder(R.drawable.ic_person_pin)
                                .error(R.drawable.ic_person_pin)
                                .circleCrop()
                                .into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.ic_person_pin);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tvEmail.setText("Unknown");
                profileImage.setImageResource(R.drawable.ic_person_pin);
            }
        });
    }

    private void fetchVideoCount() {
        DatabaseReference videosRef = FirebaseDatabase.getInstance().getReference("videos");
        videosRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long videoCount = snapshot.getChildrenCount();
                tvVideoCount.setText("Số lượng video: " + videoCount);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tvVideoCount.setText("Số lượng video: 0");
            }
        });
    }

    private void updateProfile(String userId, String newEmail) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("userprofile").child(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", newEmail);

        userRef.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                Log.e("ProfileReviewActivity", "Failed to update profile: " + error.getMessage());
            } else {
                Log.d("ProfileReviewActivity", "Profile updated successfully for userId: " + userId);
            }
        });
    }
}
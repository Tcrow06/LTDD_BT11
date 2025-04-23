package com.example.firebase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MenuDisplayActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private Uri videoUri, imageUri;
    private Button btnSelectVideo, btnUploadVideo, btnShortVideo, btnUploadImage;
    private EditText etVideoTitle, etVideoDescription;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_display);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo Firebase Database với URL đúng
        FirebaseDatabase.getInstance();

        // Liên kết với UI
        btnSelectVideo = findViewById(R.id.btnSelectVideo);
        btnUploadVideo = findViewById(R.id.btnUploadVideo);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        btnShortVideo = findViewById(R.id.btnShortVideo);
        etVideoTitle = findViewById(R.id.etVideoTitle);
        etVideoDescription = findViewById(R.id.etVideoDescription);

        // Xử lý WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Kiểm tra đăng nhập ngay khi vào activity
        checkUserLogin();

        // Sự kiện chọn video
        btnSelectVideo.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                pickVideo();
            } else {
                requestStoragePermission();
            }
        });

        // Sự kiện upload video
        btnUploadVideo.setOnClickListener(v -> uploadVideo());

        // Sự kiện upload ảnh
        btnUploadImage.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                pickImage();
            } else {
                requestStoragePermission();
            }
        });

        // Xem short video
        btnShortVideo.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Snackbar.make(v, "Bạn cần đăng nhập để xem video!", Snackbar.LENGTH_LONG).show();
                Toast.makeText(this, "Bạn cần đăng nhập để xem video!", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(MenuDisplayActivity.this, VideoShortFireBaseActivity.class);
            startActivity(intent);
        });
    }

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_IMAGES},
                    STORAGE_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Đã cấp quyền truy cập!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Bạn cần cấp đầy đủ quyền để sử dụng tính năng này!", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void checkUserLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để tiếp tục!", Toast.LENGTH_LONG).show();
            return;
        }
        updateUserProfile();
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_VIDEO_REQUEST) {
                videoUri = data.getData();
                btnUploadVideo.setEnabled(true);
                tvStatus.setText("Trạng thái: Đã chọn video, sẵn sàng upload.");
            } else if (requestCode == PICK_IMAGE_REQUEST) {
                imageUri = data.getData();
                uploadImage();
            }
        }
    }

    private void updateUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String email = currentUser.getEmail();
        String username = currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : (email != null ? email.split("@")[0] : "User");

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("userprofile").child(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("userId", userId);
        updates.put("email", email);
        updates.put("username", username);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MenuDisplayActivity", "User profile updated successfully for userId: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e("MenuDisplayActivity", "Failed to update user profile: " + e.getMessage());
                    Toast.makeText(this, "Lỗi cập nhật hồ sơ người dùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void uploadVideo() {
        String title = etVideoTitle.getText().toString().trim();
        String description = etVideoDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etVideoTitle.setError("Vui lòng nhập tiêu đề video!");
            etVideoTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etVideoDescription.setError("Vui lòng nhập mô tả video!");
            etVideoDescription.requestFocus();
            return;
        }

        if (videoUri == null) {
            View v = findViewById(R.id.main);
            Snackbar.make(v, "Vui lòng chọn video trước khi upload!", Snackbar.LENGTH_LONG).show();
            Toast.makeText(this, "Vui lòng chọn video trước khi upload!", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            View v = findViewById(R.id.main);
            Snackbar.make(v, "Bạn cần đăng nhập để upload video!", Snackbar.LENGTH_LONG).show();
            Toast.makeText(this, "Bạn cần đăng nhập để upload video!", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUploadVideo.setEnabled(false);
        tvStatus.setText("Trạng thái: Đang upload video...");

        String userId = currentUser.getUid();

        MediaManager.get().upload(videoUri)
//                .unsigned("my_unsigned_preset")
                .unsigned("save_data")
                .option("resource_type", "video")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        tvStatus.setText("Trạng thái: Bắt đầu upload video...");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        progressBar.setVisibility(View.GONE);
                        String videoUrl = resultData.get("url").toString();
                        tvStatus.setText("Trạng thái: Upload video thành công!");
                        Toast.makeText(MenuDisplayActivity.this, "Upload video thành công!", Toast.LENGTH_LONG).show();

                        Log.d("UploadVideo", "Video URL: " + videoUrl);

                        DatabaseReference videosRef = FirebaseDatabase.getInstance()
                                .getReference("videos");
                        String videoId = videosRef.push().getKey();

                        Video1Model video = new Video1Model(
                                videoId,
                                userId,
                                videoUrl,
                                title,
                                description,
                                0,
                                0
                        );

                        videosRef.child(videoId).setValue(video)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MenuDisplayActivity.this,
                                            "Đã lưu video vào database thành công!",
                                            Toast.LENGTH_LONG).show();
                                    etVideoTitle.setText("");
                                    etVideoDescription.setText("");
                                    videoUri = null;
                                    btnUploadVideo.setEnabled(false);
                                    tvStatus.setText("Trạng thái: Chưa upload");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MenuDisplayActivity.this,
                                            "Lỗi lưu video vào database: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                    btnUploadVideo.setEnabled(true);
                                });
                    }

                    @Override
                    public void onError(String requestCode, ErrorInfo error) {
                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("Trạng thái: Lỗi upload video - " + error.getDescription());
                        btnUploadVideo.setEnabled(true);
                        Toast.makeText(MenuDisplayActivity.this,
                                "Lỗi upload video: " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        tvStatus.setText("Trạng thái: Đang thử lại upload video...");
                    }
                })
                .dispatch();
    }

    private void uploadImage() {
        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh trước khi upload!", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để upload ảnh!", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Trạng thái: Đang upload ảnh...");

        String userId = currentUser.getUid();

        MediaManager.get().upload(imageUri)
                .unsigned("my_unsigned_preset")
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        tvStatus.setText("Trạng thái: Bắt đầu upload ảnh...");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        progressBar.setVisibility(View.GONE);
                        String imageUrl = resultData.get("url").toString();
                        tvStatus.setText("Trạng thái: Upload ảnh thành công!");
                        Toast.makeText(MenuDisplayActivity.this, "Upload ảnh thành công!", Toast.LENGTH_LONG).show();

                        Log.d("UploadImage", "Image URL: " + imageUrl);

                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                .getReference("userprofile").child(userId);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("profileImageUrl", imageUrl);

                        userRef.updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("UploadImage", "Successfully saved image URL to database");
                                    Toast.makeText(MenuDisplayActivity.this,
                                            "Đã lưu URL ảnh vào database thành công!",
                                            Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UploadImage", "Failed to save image URL to database: " + e.getMessage());
                                    Toast.makeText(MenuDisplayActivity.this,
                                            "Lỗi lưu URL ảnh vào database: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("Trạng thái: Lỗi upload ảnh - " + error.getDescription());
                        Toast.makeText(MenuDisplayActivity.this,
                                "Lỗi upload ảnh: " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        tvStatus.setText("Trạng thái: Đang thử lại upload ảnh...");
                    }
                })
                .dispatch();
    }
}
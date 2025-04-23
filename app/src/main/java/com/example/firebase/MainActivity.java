package com.example.firebase;


import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;



import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private VideosAdapter videosAdapter;
    private List<VideoModel> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        viewPager2 = findViewById(R.id.vpager); // Ánh xạ ViewPager2
        list = new ArrayList<>();               // Khởi tạo danh sách trống
        getVideos();                            // Gọi API lấy video
    }

    private void getVideos() {
        APIService.servieapi.getVideos().enqueue(new Callback<MessageVideoModel>() {
            @Override
            public void onResponse(Call<MessageVideoModel> call, Response<MessageVideoModel> response) {
                list = response.body().getResult(); // Nhận danh sách video từ API
                videosAdapter = new VideosAdapter(getApplicationContext(), list); // Gắn adapter
                viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);       // Scroll theo chiều dọc
                viewPager2.setAdapter(videosAdapter);
            }

            @Override
            public void onFailure(Call<MessageVideoModel> call, Throwable t) {
                Log.d("TAG", t.getMessage()); // In lỗi nếu gọi API thất bại
            }
        });
    }
}

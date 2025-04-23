package com.example.firebase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public interface APIService {

    Gson gson = new GsonBuilder().setDateFormat("yyyy MM dd HH:mm:ss").create();

    APIService servieapi = new Retrofit.Builder()
            .baseUrl("http://app.iotstar.vn:8081/appfoods/") // Base URL của API
            .addConverterFactory(GsonConverterFactory.create(gson)) // Sử dụng Gson để parse JSON
            .build()
            .create(APIService.class); // Tạo instance của API interface

    @GET("getvideos.php") // Định nghĩa endpoint
    Call<MessageVideoModel> getVideos(); // Hàm để gọi API và lấy dữ liệu video
}

package com.example.mediapipehandtracking.remote;

import com.example.mediapipehandtracking.responseClasses.ResponseClass;
import com.example.mediapipehandtracking.responseClasses.ResponseRegisterClass;
import com.example.mediapipehandtracking.responseClasses.ResponseSignalClass;
import com.example.mediapipehandtracking.responseClasses.ResponseSignalIdClass;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {

    @PUT("/addUser")
    Call<ResponseClass> addUser(@Body ResponseRegisterClass responseRegisterClass);

    @POST("/getUser")
    Call<ResponseClass> getUser(@Body ResponseRegisterClass responseRegisterClass);

    @POST("/signal/save")
    Call<ResponseSignalIdClass> save(@Body ResponseSignalClass responseSignalClass);

    @GET("/signal/ditection")
    Call<Integer> ditection(@Body ResponseSignalClass responseSignalClass);

}

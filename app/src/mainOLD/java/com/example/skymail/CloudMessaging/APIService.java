package com.example.skymail.CloudMessaging;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAOHGEhPc:APA91bHTFwwyi274AfhR9wsRzlta8QmcLUYlWxKkP94C6UVTYks7aKBLCipMUFDTIEkDKxkAMSZdEYf_URU81WEr4MlLVPj0iOnMYfaWCDEC8LFka_vXn2SjGAT3uScfi312scIAwp27" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}


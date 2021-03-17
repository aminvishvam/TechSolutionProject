package com.example.techsolutionproject.Remote;

import com.example.techsolutionproject.model.FCMResponse;
import com.example.techsolutionproject.model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAJCAbpE8:APA91bGDb5gd78LYOHEpOkce0uxgvZbeqQPbEFHKOEdJfj0nPaMB7u3nxJSfeEAvPRMt5wyRlZrZeA1ymi1VwpnFdk_roWVZBVSMQh68ndNjZ7X2XpCgYVCJgfMjI7FZrTHFi7gVXape"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
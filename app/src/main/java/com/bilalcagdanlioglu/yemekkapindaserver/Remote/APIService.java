package com.bilalcagdanlioglu.yemekkapindaserver.Remote;

import com.bilalcagdanlioglu.yemekkapindaserver.Model.MyResponse;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAbWMOo8Y:APA91bE1hJinCPrsEq1bYAcjRijsJRvqrJGnmISULngiLAqqBPhWC9t1wbpbS2MzPnkc6I-6ng4lBcrJBZfeW0OnB6krexFEoVwXQbqBmpmNVLdMnnUiiVMrxSapzp_d7hG_s61jdeah"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}

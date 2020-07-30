package com.zabih.chatBuzz.Activities.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAuLqQ63Y:APA91bGFH49Hgg2Hmc54L5uL_5q0Ol2kq3p_M6Ba4TdMJ_bkyuySrpQplgsk2tgvJiMDk6y-ihy77dASrtBRqW_tV0P7nT9kidAw76JeLt30LnDjKDX-6lBybFNs39j2-nn3pAjm-NBa"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}

package com.shipdream.lib.android.mvc.samples.simple.service.http;

import com.shipdream.lib.android.mvc.samples.simple.dto.IpPayload;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IpService {
    //https://api.ipify.org/?format=json
    @GET("/")
    Call<IpPayload> getIp(@Query("format") String format);
}

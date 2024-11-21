package com.example.freshlife;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    private static Retrofit retrofit;
//    private static final String BASE_URL = "http://10.100.102.3:5000/"; // Haifa
//    private static final String BASE_URL = "http://10.100.102.11:5000/"; // Binyamina cable
    private static final String BASE_URL = "http://192.168.0.149:5000/"; // Binyamina wifi

//    private static final String BASE_URL = "http://10.100.102.6:5000/";
//    private static final String BASE_URL = "http://" + BuildConfig.BACKEND_IP + ":5000/";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

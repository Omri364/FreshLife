package com.example.freshlife;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Properties;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides a singleton instance of Retrofit for making API requests.
 * This class is responsible for setting up the base URL and configuring
 * the Retrofit instance with the necessary converters.
 */
public class RetrofitInstance {

    private static Retrofit retrofit;
    private static final String BASE_URL = "http://10.100.102.3:5000/";

    /**
     * Returns the singleton instance of Retrofit.
     * If the instance is null, it initializes it with the base URL and Gson converter.
     *
     * @return A configured Retrofit instance.
     */
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

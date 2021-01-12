package com.example.docscanner.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofitclient = null;
    private static Retrofit getClient(){
        if(retrofitclient == null){
            retrofitclient = new Retrofit.Builder().baseUrl("http://").addConverterFactory(ScalarsConverterFactory.create()).build();
        }
        return retrofitclient;
    }
}

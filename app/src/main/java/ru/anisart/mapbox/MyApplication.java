package ru.anisart.mapbox;

import android.app.Application;

import com.google.gson.GsonBuilder;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.commons.geojson.Geometry;
import com.mapbox.services.commons.geojson.custom.GeometryDeserializer;
import com.mapbox.services.commons.geojson.custom.PositionDeserializer;
import com.mapbox.services.commons.models.Position;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyApplication extends Application {

    public static final String PUBLIC_KEY = "pk.eyJ1IjoiYW5pc2FydCIsImEiOiJjaWtsMHNuZWswMDZqdm1tNmYydWl6M2pvIn0.OFsPC78TmKIRtp9WhlsN_w";
    public static final String SECRET_KEY = "sk.eyJ1IjoiYW5pc2FydCIsImEiOiJjajR6ejE4NHUxemQwMnFvYTVnb2Q3Y3U5In0.kGC6TZOCJIj-ciZ1CIe-zg";

    private MapboxApi mapboxApi;

    @Override
    public void onCreate() {
        super.onCreate();

        Mapbox.getInstance(getApplicationContext(), PUBLIC_KEY);

        OkHttpClient.Builder httpClient =
                new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl originalHttpUrl = original.url();

                HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter("access_token", SECRET_KEY)
                        .build();

                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()
                        .url(url);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY );
        httpClient.addInterceptor(interceptor);

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl("https://api.mapbox.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(new CsvConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .registerTypeAdapter(Position.class, new PositionDeserializer())
                        .registerTypeAdapter(Geometry.class, new GeometryDeserializer())
                        .create()))
                .build();

        mapboxApi = retrofit.create(MapboxApi.class);
    }

    public MapboxApi getMapboxApi() {
        return mapboxApi;
    }
}

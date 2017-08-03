package ru.anisart.mapbox;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OverpassApi {

    @GET("datasets/v1/{username}")
    Observable<List<Dataset>> datasetsList(@Path("username") String username);
}

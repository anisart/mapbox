package ru.anisart.mapbox;

import com.mapbox.services.commons.geojson.FeatureCollection;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MapboxApi {

    @GET("datasets/v1/{username}")
    Observable<List<Dataset>> datasetsList(@Path("username") String username);

    @GET("datasets/v1/{username}/{dataset_id}/features")
    Observable<FeatureCollection> featuresList(@Path("username") String username, @Path("dataset_id") String datasetId);
}

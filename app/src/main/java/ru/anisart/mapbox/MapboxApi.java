package ru.anisart.mapbox;

import com.mapbox.services.commons.geojson.FeatureCollection;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MapboxApi {

    @GET("datasets/v1/{username}")
    Observable<List<Dataset>> datasetsList(@Path("username") String username);

    @GET("datasets/v1/{username}/{dataset_id}/features")
    Observable<FeatureCollection> featuresList(@Path("username") String username, @Path("dataset_id") String datasetId);

    @GET("https://overpass-api.de/api/interpreter?data=%5Bout%3Acsv%28%3A%3Aid%2C%22name%3Aen%22%3Bfalse%29%5D%3Brelation%5B%22admin%5Flevel%22%3D%222%22%5D%5B%22boundary%22%3D%22administrative%22%5D%5B%22ISO3166%2D1%22%7E%22%5E%2E%2E%24%22%5D%3Bout%3B%0A")
    Observable<List<OsmObject>> countriesList();

    @GET("https://overpass-api.de/api/interpreter")
    Observable<List<OsmObject>> subareasList(@Query("data") String qlRequest);
}

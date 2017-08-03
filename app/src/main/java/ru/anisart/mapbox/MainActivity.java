package ru.anisart.mapbox;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.SpatialReference;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.Geometry;
import com.mapbox.services.commons.geojson.Polygon;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;
    private List<String> exploredTiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                map.setCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(56.423040537284166, 44.65388874240455))
                        .zoom(6.0)
                        .build());
            }
        });

        exploredTiles = Arrays.asList(Mock.EXPLORER_TILES);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    public void onButtonClick(View view) {
//        ((MyApplication) getApplication()).getMapboxApi()
//                .datasetsList("anisart")
//                .map(new Function<List<Dataset>, Integer>() {
//                    @Override
//                    public Integer apply(@NonNull List<Dataset> datasets) throws Exception {
//                        System.out.println(datasets);
//                        return datasets.size();
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new DefaultObserver<Integer>() {
//                    @Override
//                    public void onNext(@NonNull Integer integer) {
//                        Toast.makeText(MainActivity.this, String.format("%d datasets", integer), Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });

        ((MyApplication) getApplication()).getMapboxApi()
                .featuresList("anisart", "cj4zz4sbt229032qn9n1bsyrl")
                .map(new Function<FeatureCollection, List<Feature>>() {
                    @Override
                    public List<Feature> apply(@NonNull FeatureCollection featureCollection) throws Exception {
                        return featureCollection.getFeatures();
                    }
                })
                .flatMap(new Function<List<Feature>, ObservableSource<Feature>>() {
                    @Override
                    public ObservableSource<Feature> apply(@NonNull List<Feature> features) throws Exception {
                        return Observable.fromIterable(features);
                    }
                })
                .filter(new Predicate<Feature>() {
                    @Override
                    public boolean test(@NonNull Feature feature) throws Exception {
                        return "Polygon".equals(feature.getGeometry().getType());
                    }
                })
//                .take(5)
                .map(new Function<Feature, String>() {
                    @Override
                    public String apply(@NonNull Feature feature) throws Exception {
                        String name = feature.getStringProperty("name");
                        System.out.println(feature.getGeometry().getType() + " - " + name);

                        String geojson = feature.getGeometry().toJson();

                        Polygon polygon = Polygon.fromJson(geojson);
                        PolygonOptions polygonOptions = new PolygonOptions()
                                .fillColor(Color.RED);
                        addPolygon(polygonOptions, polygon);

                        LatLngBounds bounds = new LatLngBounds.Builder().includes(polygonOptions.getPoints()).build();
//                        map.addPolyline(new PolylineOptions()
//                                .add(bounds.getNorthWest())
//                                .add(bounds.getNorthEast())
//                                .add(bounds.getSouthEast())
//                                .add(bounds.getSouthWest())
//                                .add(bounds.getNorthWest())
//                                .color(Color.GREEN)
//                                .width(5)
//                        );

                        SpatialReference sr = SpatialReference.create(4326);

                        com.esri.core.geometry.Geometry geometry = null;
                        try {
                            geometry = GeometryEngine.geometryFromGeoJson(
                                    geojson, 0, com.esri.core.geometry.Geometry.Type.Polygon)
                                    .getGeometry();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        int x1 = lon2tile(bounds.getLonWest(), 14);
                        int x2 = lon2tile(bounds.getLonEast(), 14);
                        int y1 = lat2tile(bounds.getLatNorth(), 14);
                        int y2 = lat2tile(bounds.getLatSouth(), 14);

                        int tilesCount = 0;
                        int exploredCount = 0;
                        for (int x = x1; x < x2 + 1; x++) {
                            for (int y = y1; y < y2 + 1; y++) {
                                BoundingBox bb = tile2boundingBox(x, y, 14);
                                com.esri.core.geometry.Polygon p = new com.esri.core.geometry.Polygon();
                                p.startPath(bb.west, bb.north);
                                p.lineTo(bb.east, bb.north);
                                p.lineTo(bb.east, bb.south);
                                p.lineTo(bb.west, bb.south);
                                if (!GeometryEngine.disjoint(geometry, p, sr)) {
                                    tilesCount++;
                                    if (exploredTiles.contains(String.format("%d-%d", x, y))) {
                                        exploredCount++;
                                    }
//                                    map.addPolygon(new PolygonOptions()
//                                            .add(new LatLng(bb.north, bb.west))
//                                            .add(new LatLng(bb.north, bb.east))
//                                            .add(new LatLng(bb.south, bb.east))
//                                            .add(new LatLng(bb.south, bb.west))
//                                            .add(new LatLng(bb.north, bb.west))
//                                            .fillColor(Color.parseColor("#30FF0000"))
//                                    );
                                }
                            }
                        }

                        float coverage = exploredCount / (float) tilesCount;
                        polygonOptions.alpha(coverage);
                        map.addPolygon(polygonOptions);

                        return name;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<String>() {
                    @Override
                    public void onNext(@NonNull String string) {
                        Toast.makeText(MainActivity.this, string + " was finished", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void addPolygon(PolygonOptions options, Polygon polygon) {
        List<LatLng> list = new ArrayList<>();
        for (int i = 0; i < polygon.getCoordinates().size(); i++) {
            for (int j = 0; j < polygon.getCoordinates().get(i).size(); j++) {
                list.add(new LatLng(
                        polygon.getCoordinates().get(i).get(j).getLatitude(),
                        polygon.getCoordinates().get(i).get(j).getLongitude()
                ));
            }
        }
        options.addAll(list);
    }

    public void onButton2Click(View view) {
        startActivity(new Intent(this, Main2Activity.class));
    }

    class BoundingBox {
        double north;
        double south;
        double east;
        double west;
    }
    BoundingBox tile2boundingBox(final int x, final int y, final int zoom) {
        BoundingBox bb = new BoundingBox();
        bb.north = tile2lat(y, zoom);
        bb.south = tile2lat(y + 1, zoom);
        bb.west = tile2lon(x, zoom);
        bb.east = tile2lon(x + 1, zoom);
        return bb;
    }

    static double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    static double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    static int lon2tile(double lon, int zoom) {
        return (int) Math.floor((lon + 180) / 360 * Math.pow(2, zoom));
    }
    static int lat2tile(double lat, int zoom) {
        return (int) Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180)
                + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI)
                / 2 * Math.pow(2, zoom));
    }
}

package ru.anisart.mapbox;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class CsvConverterFactory extends Converter.Factory {

    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return CsvConverter.INSTANCE;
    }

    final static class CsvConverter implements Converter<ResponseBody, List<OsmObject>> {
        static final CsvConverter INSTANCE = new CsvConverter();


        @Override
        public List<OsmObject> convert(ResponseBody value) throws IOException {
            List<OsmObject> countries = new ArrayList<>();
            String response = value.string();
            if (response != null) {
                String[] lines = response.split("\n");
                for (String line : lines) {
                    String[] data = line.split("\t");
                    countries.add(new OsmObject(Integer.valueOf(data[0]), data[1]));
                }
            }
            return countries;
        }
    }
}

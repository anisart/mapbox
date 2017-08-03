package ru.anisart.mapbox

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast


class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
    }

    fun onButtonClick(button: View) {
        (application as MyApplication).mapboxApi
                .overpass()
                .flatMapIterable { t -> t }
                .map { t -> "%s - %d".format(t.name, t.id) }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{countries ->
                    val lvMain = findViewById(R.id.countries) as ListView

                    val adapter = ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, countries)
                    lvMain.adapter = adapter
                    lvMain.setOnItemClickListener { parent, view, position, id ->
                        Toast.makeText(this, adapter.getItem(position), Toast.LENGTH_SHORT).show() }
                }
    }
}

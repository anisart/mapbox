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

    val QL_SUBAREAS = """[out:csv(::id,"name";false)];relation(%s);relation(r:"subarea");out;"""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
    }

    fun onButtonClick(button: View) {
        (application as MyApplication).mapboxApi
                .countriesList()
                .flatMapIterable { it }
                .map { (id, name) -> "%s - %d".format(name, id) }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ countries ->
                    val lvMain = findViewById(R.id.countries) as ListView
                    val adapter = ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, countries.sorted())
                    lvMain.adapter = adapter
                    lvMain.setOnItemClickListener { _, _, position, id ->
                        val country = adapter.getItem(position)
                        Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show()
                        loadSubareas(country.substringAfter("- "))
                    }
                }, {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                })
    }

    fun loadSubareas(areaId: String) {
        (application as MyApplication).mapboxApi
                .subareasList(QL_SUBAREAS.format(areaId))
                .flatMapIterable { it }
                .map { (id, name) -> "%s - %d".format(name, id) }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ subareas ->
                    val lvMain = findViewById(R.id.countries) as ListView
                    val adapter = ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, subareas.sorted())
                    lvMain.adapter = adapter
                    lvMain.setOnItemClickListener { _, _, position, id ->
                        val subarea = adapter.getItem(position)
                        Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show()
                        loadSubareas(subarea.substringAfter("- "))
                    }
                }, {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
                )
    }
}

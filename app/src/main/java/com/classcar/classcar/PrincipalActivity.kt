package com.classcar.classcar

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class PrincipalActivity : AppCompatActivity(),OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var selectedDate: String
    private lateinit var tvDate: TextView
    private lateinit var txtCantidad: EditText
    private var cantidad: Int = 0
    val coordinates = mutableListOf<Pair<Double, Double>>() // Cordenadas de la ruta
    lateinit var start: String
    lateinit var end: String
    var poly: Polyline? = null //Variable para poder dibujar en el mapa
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        // SearchView para el origen
        val searchViewOrigen = findViewById<SearchView>(R.id.searchViewOrigen)
        searchViewOrigen.queryHint = "Selecciona tu ubicación"
        searchViewOrigen.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus && searchViewOrigen.queryHint == "Selecciona tu ubicación") {
                searchViewOrigen.queryHint = ""
            } else if (!hasFocus && searchViewOrigen.queryHint == "") {
                searchViewOrigen.queryHint = "Selecciona tu ubicación"
            }
        }

        // SearchView para el destino
        val searchViewDestino = findViewById<SearchView>(R.id.searchViewDestino)
        searchViewDestino.queryHint = "Selecciona el destino"
        searchViewDestino.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus && searchViewDestino.queryHint == "Selecciona el destino") {
                searchViewDestino.queryHint = ""
            } else if (!hasFocus && searchViewDestino.queryHint == "") {
                searchViewDestino.queryHint = "Selecciona el destino"
            }
        }
        tvDate = findViewById(R.id.tvDate)
        txtCantidad = findViewById(R.id.etQuantity)
        val btnCleanRoute = findViewById<Button>(R.id.btnCleanRoute)
        val btnSelectRoute = findViewById<Button>(R.id.btnSelectRoute)
        val btnBuscar = findViewById<Button>(R.id.btnBuscar)
        val geocoder = Geocoder(this, Locale.getDefault())
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchViewOrigen.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { //Obtiene el texto
                query?.let {
                    val addresses = geocoder.getFromLocationName(it, 1) //direccion, it es la query
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            val location = addresses[0]
                            val latitude = location.latitude
                            val longitude = location.longitude
                            coordinates.add(Pair(longitude, latitude))
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng( //move camera para acercar el mapa a la ruta
                                latitude,
                                longitude
                            ), 10f))
                            Toast.makeText(this@PrincipalActivity, "Punto de origen seleccionado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@PrincipalActivity, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        searchViewDestino.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val addresses = geocoder.getFromLocationName(it, 1)
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            val location = addresses[0]
                            val latitude = location.latitude
                            val longitude = location.longitude
                            coordinates.add(Pair(longitude, latitude) as Pair<Double, Double>)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 10f))
                            Toast.makeText(this@PrincipalActivity, "Punto de destino seleccionado", Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(this@PrincipalActivity, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }
    private fun getRetrofit(): Retrofit { //Funcion para redirigir
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private fun crearRuta(){

    }
    override fun onMapReady(map: GoogleMap) {
        this.map = map
    }
}
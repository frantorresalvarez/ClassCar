package com.classcar.classcar

import android.app.DatePickerDialog
import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.classcar.classcar.rutas.ApiService
import com.classcar.classcar.rutas.RouteResponse
import com.classcar.classcar.vistaviajes.PublicarActivity
import com.classcar.classcar.vistaviajes.ViajesActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.locks.Lock

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

        //Redireccion Menu
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this,PrincipalActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_viajes -> {
                    // Mostrar fragmento de búsqueda
                    val intent = Intent(this, ViajesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_publicar -> {
                    val intent = Intent(this, PublicarActivity::class.java)
                    startActivity(intent)
                    // Iniciar actividad de perfil
                    true
                }
                R.id.info_universidades -> {
                    // Iniciar actividad de perfil
                    val intent = Intent(this,UniActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

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
        //val btnSelectRoute = findViewById<Button>(R.id.btnSelectRoute)
        val btnBuscar = findViewById<Button>(R.id.btnBuscar)
        val geocoder = Geocoder(this, Locale.getDefault())
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Borrado de la ruta
        btnCleanRoute.setOnClickListener {
            start = ""
            end = ""
            coordinates.clear()
            poly?.remove()
            poly = null
            Toast.makeText(this,"Selecciona tu ubicación y tu destino",Toast.LENGTH_SHORT).show()
        }
        tvDate.setOnClickListener{showDatePickerDialog()}
        btnBuscar.setOnClickListener {
            val cantidadtxt = txtCantidad.text.toString()
            if(cantidadtxt.isNotEmpty() && cantidadtxt.toIntOrNull() != null){
                cantidad = cantidadtxt.toInt()
            }
            val intent = Intent(this@PrincipalActivity, ViajesActivity::class.java)
            startActivity(intent)  // Iniciar la actividad
            redirigir(cantidad)    // Llamar a la función después de iniciar la actividad
        }


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
                            if(coordinates.size==2){
                                crearRuta(coordinates)
                            }

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
    //Funcion para redirigir
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    fun showDatePickerDialog(){
        val calendar = Calendar.getInstance()
        val anios = calendar.get(Calendar.YEAR)
        val mes = calendar.get(Calendar.MONTH)
        val dia = calendar.get(Calendar.DAY_OF_MONTH)
        val dataPickerDialog = DatePickerDialog(this,
                {_,selectYear,selectMonth,selectDayOfMonth ->
                    selectedDate = "${pad(selectDayOfMonth)}/${pad(selectMonth)}/${pad(selectYear)}"
                    tvDate.text = selectedDate
                }, anios, mes, dia
            )
        dataPickerDialog.show()
    }
    fun redirigir(cantidad:Int){
        Log.d("resultado","coordenadas ${start} ${end} ${selectedDate} $cantidad")
    }
    private fun pad(number:Int):String{
        return if (number<10) "0$number" else "$number"
    }
    private fun crearRuta(coordenadas:List<Pair<Double, Double>>){
        start = "${coordenadas[0].first},${coordenadas[0].second}"
        end = "${coordenadas[1].first},${coordenadas[1].second}"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = getRetrofit().create(ApiService::class.java).getRoute("5b3ce3597851110001cf62489da8d5ab4afb4b4897424d97297ffdc2",start,end)
                if(call.isSuccessful){
                    drawRoute(call.body())
                }else{
                    Toast.makeText(this@PrincipalActivity, "Error al crear la ruta", Toast.LENGTH_SHORT).show()
                }
            }catch(e:Exception){
                Log.e("Respuesta de la ruta","${e.message}")
            }
        }
    }
    //Funcion para dibujar en el Mapa
    private fun drawRoute(routeResponse: RouteResponse?) {
        val polyLineOptions = PolylineOptions()
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            polyLineOptions.add(LatLng(it[1], it[0]))
        }
        runOnUiThread {
            poly = map.addPolyline(polyLineOptions)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
    }

}
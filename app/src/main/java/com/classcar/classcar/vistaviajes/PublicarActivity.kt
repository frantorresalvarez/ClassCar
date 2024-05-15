package com.classcar.classcar.vistaviajes

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.classcar.classcar.PrincipalActivity
import com.classcar.classcar.R
import com.classcar.classcar.UniActivity
import com.classcar.classcar.modelos.Viaje
import com.classcar.classcar.rutas.ApiService
import com.classcar.classcar.rutas.RouteResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.locks.Lock

class PublicarActivity : AppCompatActivity(),OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var selectedDate: String
    private lateinit var selectedTime: String
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var txtCantidad: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var origen: String
    private lateinit var destino: String
    private var cantidad: Int = 0
    val coordinates = mutableListOf<Pair<Double, Double>>() // Cordenadas de la ruta
    lateinit var start: String
    lateinit var end: String
    var poly: Polyline? = null //Variable para poder dibujar en el mapa
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicar)

        //Redireccion Menu
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, PrincipalActivity::class.java)
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

        auth = Firebase.auth
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
        tvTime = findViewById(R.id.tvHour)
        txtCantidad = findViewById(R.id.etQuantity)
        val btnCleanRoute = findViewById<Button>(R.id.btnCleanRoute)

        val btnPublicar = findViewById<Button>(R.id.btnPublicar)
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
        btnPublicar.setOnClickListener {
            val cantidadtxt = txtCantidad.text.toString()
            if(cantidadtxt.isNotEmpty()&&cantidadtxt.toIntOrNull()!= null){
                cantidad = cantidadtxt.toInt()
            }
            registroViaje()
        }
        tvTime.setOnClickListener{showTime()}

        searchViewOrigen.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { //Obtiene el texto
                query?.let {
                    origen = it
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
                            Toast.makeText(this@PublicarActivity, "Punto de origen seleccionado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@PublicarActivity, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
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
                    destino = it
                    val addresses = geocoder.getFromLocationName(it, 1)
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            val location = addresses[0]
                            val latitude = location.latitude
                            val longitude = location.longitude
                            coordinates.add(Pair(longitude, latitude) as Pair<Double, Double>)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 10f))
                            Toast.makeText(this@PublicarActivity, "Punto de destino seleccionado", Toast.LENGTH_SHORT).show()
                            if(coordinates.size==2){
                                crearRuta(coordinates)
                            }

                        } else {
                            Toast.makeText(this@PublicarActivity, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
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
    // Funcion de Obtener la fecha
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
    //Funcion para obtener tiempo
    fun showTime(){
        val calendar = Calendar.getInstance()
        val hora = calendar.get(Calendar.HOUR_OF_DAY)
        val minutos = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this,
            {_,selectHour,selectMinute ->
                selectedTime = "${pad(selectHour)}:${pad(selectMinute)}"
                tvTime.text = selectedTime
            }, hora, minutos, true
        )
        timePickerDialog.show()
    }
    fun redirigir(cantidad:Int){
        Log.d("resultado","coordenadas ${start} ${end} ${selectedDate} $cantidad")
    }
    //Funcion para registrar el viaje
    fun registroViaje(){
        val user = auth.currentUser
        if(user != null){
            val userId = user.uid //unique id
            val db = Firebase.firestore // firestore es la bd no relacionada
            val viajeCollection = db.collection("Viaje").document()
            val viajeData = hashMapOf(
                "idUsuario" to userId, "Origen" to start, "Destino" to end, "fecha" to selectedDate,
                "Hora" to selectedTime, "Pasajeros" to cantidad, "origentxt" to origen, "destinotxt" to destino
            )
            viajeCollection.set(viajeData).addOnSuccessListener {
                Toast.makeText(this,"Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ViajesActivity::class.java)
                startActivity(intent)
            }.addOnFailureListener{
                Toast.makeText(this,"Fallo al ingresar tus datos", Toast.LENGTH_SHORT).show()
            }
        }

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
                    Toast.makeText(this@PublicarActivity, "Error al crear la ruta", Toast.LENGTH_SHORT).show()
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
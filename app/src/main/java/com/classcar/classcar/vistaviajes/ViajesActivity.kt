package com.classcar.classcar.vistaviajes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.classcar.classcar.R
import com.classcar.classcar.adapter.ViajeAdapter
import com.classcar.classcar.modelos.Viaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViajesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viajeAdapter: ViajeAdapter
    private lateinit var viajesList: MutableList<Viaje>
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viajes)
        auth = Firebase.auth
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        viajesList = mutableListOf()
        viajeAdapter = ViajeAdapter(viajesList, this)
        recyclerView.adapter = viajeAdapter
        obtenerViajes()
    }
    fun obtenerViajes(){
        val db = Firebase.firestore
        val user = auth.currentUser
        if(user != null){
            val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time) //Obtener la fecha actual del dispositivo
            db.collection("Viaje")
                .whereGreaterThanOrEqualTo("fecha",fechaActual) // Desde la fecha actual en adelante
                .get()
                .addOnSuccessListener { documents ->
                    viajesList.clear()
                    for (document in documents){
                        val viaje = document.toObject(Viaje::class.java)
                        viajesList.add(viaje)
                    }
                    viajeAdapter.notifyDataSetChanged()
                }.addOnFailureListener{Toast.makeText(this,"Error al cargar el viaje",Toast.LENGTH_SHORT).show()}
        }
    }
}
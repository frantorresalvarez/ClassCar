package com.classcar.classcar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnregistrarse = findViewById<Button>(R.id.btn_registrarse)
        btnregistrarse.setOnClickListener {
            val intent = Intent(this,RegistroActivity::class.java)
            this.startActivity(intent)
        }
        val btn_iniciar_sesion = findViewById<Button>(R.id.btn_iniciar_sesion)
        btn_iniciar_sesion.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            this.startActivity(intent)
        }
    }
}
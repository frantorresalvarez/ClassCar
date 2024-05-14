package com.classcar.classcar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        val txtEmail = findViewById<EditText>(R.id.txtEmail)
        val txtPassword = findViewById<EditText>(R.id.txtPassword)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)
        btnIniciarSesion.setOnClickListener {
            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()

            if(email.isEmpty()||password.isEmpty()){
                Toast.makeText(this,"Este campo no puede estar vacio", Toast.LENGTH_SHORT).show()
            }else{
                InicioSesion(email, password)
            }
        }
    }
    fun InicioSesion(email:String,password:String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task ->
                if(task.isSuccessful){
                    Toast.makeText(this,"Acceso Permitido", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,PrincipalActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this,"Acceso Denegado", Toast.LENGTH_SHORT).show()
                }

            }

    }
}
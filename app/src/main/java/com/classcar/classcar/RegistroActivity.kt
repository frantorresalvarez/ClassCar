package com.classcar.classcar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = Firebase.auth
        val btnRegistro = findViewById<Button>(R.id.btnRegistro)
        val txtNombre = findViewById<EditText>(R.id.txtNombre)
        val txtEmail = findViewById<EditText>(R.id.txtEmail)
        val txtPassword = findViewById<EditText>(R.id.txtPassword)
        val txtRepit = findViewById<EditText>(R.id.txtRepit)


        btnRegistro.setOnClickListener {
            val nombre = txtNombre.text.toString()
            val txtEmail = txtEmail.text.toString()
            val txtPassword = txtPassword.text.toString()
            val txtRepit = txtRepit.text.toString()
            if(nombre.isEmpty()||txtEmail.isEmpty()||txtPassword.isEmpty()||txtRepit.isEmpty()){
                Toast.makeText(this,"Este campo no puede estar vacio",Toast.LENGTH_SHORT).show()
            }else{
                registroUsuario(nombre,txtEmail,txtPassword)

            }
        }
    }
    fun registroUsuario(nombre: String, email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
            task ->
            if(task.isSuccessful){
                val user = auth.currentUser
                if(user != null){
                    val userid = user.uid
                    val db = Firebase.firestore
                    val userRef = db.collection("usuario").document(userid)
                    val userData = hashMapOf(
                        "idUsuario" to userid,
                        "nombreUsuario" to nombre,
                        "emailUsuario" to email
                    )
                    userRef.set(userData).addOnSuccessListener {
                        Toast.makeText(this, "Datos guardados correctamente en Firestore", Toast.LENGTH_SHORT).show()

                    }
                        .addOnFailureListener{
                            Toast.makeText(this, "Error al guardar los datos del usuario en Firestore", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}
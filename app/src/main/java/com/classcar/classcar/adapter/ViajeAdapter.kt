package com.classcar.classcar.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowId
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.classcar.classcar.R
import com.classcar.classcar.modelos.Viaje
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception


class ViajeAdapter(private val viajes:List<Viaje>,private val context: Context):RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViajeViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_viaje,parent,false)
        return ViajeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViajeViewHolder, position: Int) {
        val viajeItem = viajes[position]
        holder.txtStart.text = viajeItem.origentxt
        holder.txtEnd.text = viajeItem.destinotxt
        holder.txtFecha.text = viajeItem.fecha
        holder.txtHora.text = viajeItem.Hora
        holder.txtPasajeros.text = viajeItem.Pasajeros.toString()
        holder.itemView.setOnClickListener{obtenerUsuario(
            viajeItem.idUsuario, { nombre, email ->
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Información del usuario")
                    .setMessage("nombre: $nombre \n email: $email")
                    .setPositiveButton("cerrar"){dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }
        )}

    }

    override fun getItemCount() = viajes.size

    class ViajeViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val txtStart:TextView = itemView.findViewById(R.id.tvStart)
        val txtEnd:TextView = itemView.findViewById(R.id.tvEnd)
        val txtFecha:TextView = itemView.findViewById(R.id.tvFecha)
        val txtHora:TextView = itemView.findViewById(R.id.tvHora)
        val txtPasajeros:TextView = itemView.findViewById(R.id.tvPasajeros)
    }

    fun obtenerUsuario(id: String, onSucces: usuarioExitoso){
        val db = Firebase.firestore
        val usuario = db.collection("usuario").document(id)
        usuario.get()
            .addOnSuccessListener { document ->
                if(document != null){
                    val nombre = document.getString("nombreUsuario")
                    val email = document.getString("emailUsuario")
                    if(nombre != null && email != null){
                        onSucces(nombre, email)
                    }
                }
            }
    }
}

typealias usuarioExitoso = (nombre:String, email:String) -> Unit
typealias usuarioError = (exception: Exception) -> Unit
typealias usuarioNoencontrado = () -> Unit
package mx.edu.ittepic.ladm_u4_practica1_almacensms

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import mx.edu.ittepic.ladm_u4_practica1_almacensms.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    val siPermiso = 1
    val siPermisoRecived = 2
    val c = Calendar.getInstance()
    var baseRemota = Firebase.database.reference
    var listaIDs = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //-----Snapshot de recuperacion de datos
        val consulta = FirebaseDatabase.getInstance().getReference().child("mensajes")

        val postListener = object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var datos = ArrayList<String>()
                listaIDs.clear()
                for (data in snapshot.children!!){
                    val id = data.key
                    listaIDs.add(id!!)
                    val numeroTMensaje = data.getValue<Message>()!!.phonenumber
                    val horaMensaje = data.getValue<Message>()!!.messageHour
                    val fechaMensaje = data.getValue<Message>()!!.messageDate
                    datos.add("\n Numero Telefonico: ${numeroTMensaje} \n " +
                            "Hora del mensaje: ${horaMensaje} \n" +
                            "Fecha del mensaje: ${fechaMensaje} \n" +
                            "")
                }
                mostrarLista(datos)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        consulta.addValueEventListener(postListener)
        //--------------------------------------

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECEIVE_SMS),siPermisoRecived)
        }
        binding.button.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.SEND_SMS),siPermiso)
            }else{
                envioSMS()
            }
        }
    }

    private fun mostrarLista(datos: ArrayList<String>) {
        binding.listaMensajes.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,datos)

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == siPermiso){
            envioSMS()
        }
        if(requestCode == siPermisoRecived){
            mensajeRecibir()
        }
    }

    private fun mensajeRecibir() {
        AlertDialog.Builder(this).setMessage("Se otorgo recibir").show()
    }

    private fun envioSMS() {
        SmsManager.getDefault().sendTextMessage(binding.numero.text.toString(),null,binding.mensaje.text.toString()
            ,null,null)
        Toast.makeText(this,"Mensaje enviado correctamente", Toast.LENGTH_LONG).show()
        //Parte de la insercion en realtime
        var year = c.get(Calendar.YEAR).toString()
        var month = c.get(Calendar.MONTH).toString()
        var day = c.get(Calendar.DAY_OF_MONTH).toString()

        var complete_date = year + "/"+ month+ "/"+day

        var hour = c.get(Calendar.HOUR_OF_DAY).toString()
        var minute = c.get(Calendar.MINUTE).toString()

        var complete_hour = hour+ ":" +minute
        val message = Message(binding.numero.text.toString(),complete_date,complete_hour)

        baseRemota.child("mensajes").push().setValue(message)
            .addOnSuccessListener {
                AlertDialog.Builder(this)
                    .setMessage("Se inserto correctamente")
                    .show()
                limpiarCampos();
            }.addOnFailureListener{
                AlertDialog.Builder(this)
                    .setMessage(it.message)
                    .setPositiveButton("Ok"){d,i ->}
                    .show()
            }
    }
    private fun limpiarCampos(){
        binding.numero.text.clear();
        binding.mensaje.text.clear();
    }
}
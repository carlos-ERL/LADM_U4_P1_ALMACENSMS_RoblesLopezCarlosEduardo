package mx.edu.ittepic.ladm_u4_practica1_almacensms

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.telephony.SmsManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.firebase.database.BuildConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import mx.edu.ittepic.ladm_u4_practica1_almacensms.databinding.ActivityMainBinding
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedWriter
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    val siPermiso = 1
    val siPermisoRecived = 2
    val c = Calendar.getInstance()
    var baseRemota = Firebase.database.reference
    var listaIDs = ArrayList<String>()
    var datos = ArrayList<String>()
    var datosCSV = ArrayList<Message>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //-----Snapshot de recuperacion de datos
        val consulta = FirebaseDatabase.getInstance().getReference().child("mensajes")

        val postListener = object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                listaIDs.clear()
                datos.clear()
                datosCSV.clear()
                for (data in snapshot.children!!){
                    val id = data.key
                    listaIDs.add(id!!)
                    val registroCompleto = data.getValue<Message>()!!
                    val numeroTMensaje = data.getValue<Message>()!!.phonenumber
                    val horaMensaje = data.getValue<Message>()!!.messageHour
                    val fechaMensaje = data.getValue<Message>()!!.messageDate
                    datos.add("\nNumero Telefonico: ${numeroTMensaje}\n" +
                            "Hora del mensaje: ${horaMensaje}\n" +
                            "Fecha del mensaje: ${fechaMensaje}\n" +
                            "")
                    datosCSV.add(registroCompleto)
                }
                mostrarLista(datos)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        consulta.addValueEventListener(postListener)
        //--------------------------------------

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS),siPermisoRecived)
        }
        binding.button.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.SEND_SMS),siPermiso)
            }else{
                envioSMS()
            }
        }

        binding.descargar.setOnClickListener {
            if (datos.isNotEmpty()){
                crearArchivoCSV(datosCSV)
                val path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
                )

                val fileName = "ListaDeMensajes.csv"
                val file = File(path, "/$fileName")

                Toast.makeText(this,
                    "Archivo descargado en: \n$file",
                    Toast.LENGTH_LONG).show()
            } else{
                Toast.makeText(this,
                    "No hay datos que descargar",
                    Toast.LENGTH_LONG).show()
            }
        }

        binding.botonAbrir.setOnClickListener {
            try {
                abrirCSV()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error al abrir el archivo",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun abrirCSV () {
        try {

            val path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
            )

            val fileName = "ListaDeMensajes.csv"
            val file = File(path, "/$fileName")

            val csvIntent = Intent(Intent.ACTION_VIEW)
            csvIntent.setDataAndType(
                FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file),
                "text/csv"
            )

            csvIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            csvIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            this.startActivity(csvIntent)
        } catch (e: java.lang.Exception) {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("No se encontro ninguna aplicación pata abrir su archivo. Asegurese de contar un una  instalado")
                .setNeutralButton("Ok") { _, _ ->
                }.show()
            e.printStackTrace()

        }
    }
    private fun crearArchivoCSV(datos: ArrayList<Message>) {
        val path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS
        )

        val fileName = "ListaDeMensajes.csv"
        val file = File(path, "/$fileName")

        EsribirCSV(file, datos)
    }

    private fun EsribirCSV(file: File, datos : ArrayList<Message>) {
        val writer = BufferedWriter(file.bufferedWriter())

        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader("Numero de telefono", "Fecha", "Mensaje"));

        for (smsMessage in datos) {

            val smsMessageData = listOf(
                smsMessage.phonenumber.toString(),
                smsMessage.messageHour.toString(),
                smsMessage.messageDate.toString()
            )
            csvPrinter.printRecord(smsMessageData)
        }
        csvPrinter.flush()
        csvPrinter.close()
    }

    private fun mostrarLista(datos: ArrayList<String>) {
        binding.listaMensajes.adapter = ArrayAdapter<String>(this, androidx.appcompat.R.layout.select_dialog_multichoice_material,datos)
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
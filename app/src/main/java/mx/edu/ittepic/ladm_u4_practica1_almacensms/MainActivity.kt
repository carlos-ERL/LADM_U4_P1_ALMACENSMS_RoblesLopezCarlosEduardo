package mx.edu.ittepic.ladm_u4_practica1_almacensms

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import mx.edu.ittepic.ladm_u4_practica1_almacensms.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    val siPermiso = 1
    val siPermisoRecived = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
    }
}
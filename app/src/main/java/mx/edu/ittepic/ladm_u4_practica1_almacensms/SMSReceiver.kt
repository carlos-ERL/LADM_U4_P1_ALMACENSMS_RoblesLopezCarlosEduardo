package mx.edu.ittepic.ladm_u4_practica1_almacensms
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.widget.Toast

/*
* RECIEVER = Evento u oyente de android que permite la lectura de eventos
* del sistema operativo
* */
class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras
        if(extras != null){
            var sms = extras.get("pdus") as Array<Any>
            for(indice in sms.indices){
                val formato = extras.getString("format")
                var smsMensaje = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    SmsMessage.createFromPdu( sms[indice] as ByteArray,formato)
                }else{
                    SmsMessage.createFromPdu( sms[indice] as ByteArray)
                }
                var celularOrgigen = smsMensaje.originatingAddress
                var contenidoSMS = smsMensaje.messageBody.toString()

                Toast.makeText(context,"Entro contenido ${contenidoSMS}",Toast.LENGTH_LONG).show()
            }
        }



    }
}
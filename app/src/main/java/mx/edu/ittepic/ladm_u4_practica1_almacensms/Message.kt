package mx.edu.ittepic.ladm_u4_practica1_almacensms

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message(val phonenumber: String? = "",  val messageDate: String?= "",val messageHour: String?="",) {

}
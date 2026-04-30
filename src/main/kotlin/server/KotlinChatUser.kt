package com.lucaslpmoura.kotlin_chat.server

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class KotlinChatUser(id: String, name: String, val socket: Socket,  mediator: UserRoomMediatorInteface)
    : KotlinChatComponent(id, name, mediator)
{
    val address : String = socket.inetAddress?.toString() ?: ""
    var input : InputStream? = null
    var output : OutputStream? = null


    init {
        try{
            input  = socket.getInputStream()
            output = socket.getOutputStream()
        }catch (e: IOException){
            println("Failed get I/O streams for user $id")
            e.printStackTrace()
        }
    }




}
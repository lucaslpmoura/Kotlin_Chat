package com.lucaslpmoura.kotlin_chat.server

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class KotlinChatUser(id: String, name: String, val socket: Socket,  mediator: UserRoomMediatorInterface)
    : KotlinChatComponent(id, name, mediator)
{
    val address : String = socket.inetAddress?.toString() ?: ""
    var input : InputStream? = socket.getInputStream()
    var output : OutputStream? = socket.getOutputStream()


}
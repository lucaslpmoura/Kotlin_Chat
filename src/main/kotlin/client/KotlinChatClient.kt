package com.lucaslpmoura.kotlin_chat.client

import java.net.Socket

class KotlinChatClient {

    val serverAddress = "localhost"
    val serverPort = 7960

    lateinit var socket : Socket

    lateinit var name : String
    public fun run(){
        while(true) {
            socket = Socket(serverAddress, serverPort)
            print("Type your username: ")
            name = readln()
            socket.outputStream.write(name.toByteArray())
        }
    }

}
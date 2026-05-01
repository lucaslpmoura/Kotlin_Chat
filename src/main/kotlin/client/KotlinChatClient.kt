package com.lucaslpmoura.kotlin_chat.client

import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage
import com.lucaslpmoura.kotlin_chat.common.MAX_MESSAGE_SIZE
import com.lucaslpmoura.kotlin_chat.common.getMessageFromBytes
import com.lucaslpmoura.kotlin_chat.common.parseDataFromServerMessage
import com.lucaslpmoura.kotlin_chat.common.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.Socket
import kotlin.time.Duration.Companion.milliseconds

class KotlinChatClient {

    val serverAddress = "localhost"
    val serverPort = 7960

    lateinit var socket : Socket

    @Volatile
    var isTCPConnected: Boolean = false

    @Volatile
    var isConnected : Boolean = false

    var name : String? = null
    var id : String? = null

    private val tcpConnectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val readScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    public fun run(){

        tcpConnectionScope.launch {
            while (!isTCPConnected) {
                try {
                    socket = Socket(serverAddress, serverPort)
                    isTCPConnected = true
                } catch (e: IOException) {
                    println("Failed to connect to the server: ${e.message}")
                }
            }
            println("Connected to server at $serverAddress")
        }


        readScope.launch {
            while (true) {
                if(isTCPConnected) {
                    val message = readMessage()
                    processMessage(message)
                }else{
                    delay(100.milliseconds)
                }
            }
        }
    }

    public fun sendConnectMessage(name : String) {
        if(!isTCPConnected) throw IOException("Socket is not connected.")
        val message = KotlinChatMessage("SELF", KotlinChatMessage.Type.CONNECT, name)
        socket.outputStream.write(message.toByteArray())
    }

    private fun readMessage() : KotlinChatMessage {
        val buffer = ByteArray(MAX_MESSAGE_SIZE)
        try{
            socket.inputStream.read(buffer)

        }catch (e: IOException){
            println("Failed to read from server: ${e.message}")
        }
        return getMessageFromBytes("SERVER", buffer)
    }

    private fun processMessage(message: KotlinChatMessage) {
        when(message.type) {
            KotlinChatMessage.Type.CONNECT -> {
                processConnectMessage(message)
            }
            else -> TODO()
        }

        if(id != null){
            isConnected = true
            println("Connected to chat with id $id")
        }
    }

    private fun processConnectMessage(message: KotlinChatMessage) {
        try{
            val id = parseDataFromServerMessage(message)["id"]
            this.id = id
        }catch (e: Exception){
            println("Error parsing CONNECT message: ${e.message}")
        }
    }

}
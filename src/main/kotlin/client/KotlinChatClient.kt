package com.lucaslpmoura.kotlin_chat.client

import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage
import com.lucaslpmoura.kotlin_chat.common.MAX_MESSAGE_SIZE
import com.lucaslpmoura.kotlin_chat.common.getMessageFromBytes
import com.lucaslpmoura.kotlin_chat.common.parseDataFromServerMessage
import com.lucaslpmoura.kotlin_chat.common.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.Socket
import java.nio.charset.Charset
import kotlin.time.Duration.Companion.milliseconds

class KotlinChatClient {

    val serverAddress = "localhost"
    val serverPort = 7960

    private lateinit var socket : Socket

    @Volatile
    private var isTCPConnected: Boolean = false

    @Volatile
    private var isConnected : Boolean = false

    var name : String? = null
        private set
    var id : String? = null
        private set

    var lastError : KotlinChatMessage? = null

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

    public fun connect(name : String) {
        if(!isTCPConnected) throw IOException("Socket is not connected.")
        val message = KotlinChatMessage("SELF", KotlinChatMessage.Type.CONNECT, name)
        socket.outputStream.write(message.toByteArray())
    }

    public suspend fun disconnect(disconnectId : String = id!!) {
        if(!isTCPConnected) throw IOException("Socket is not connected.")
        val message = KotlinChatMessage("SELF", KotlinChatMessage.Type.DISCONNECT, disconnectId ?: throw IOException("Id is not set."))
        println(message.toByteArray().toString(Charsets.UTF_8))
        socket.outputStream.write(message.toByteArray())
        delay(100.milliseconds)
        processDisconnect()
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
                processConnect(message)
            }
            KotlinChatMessage.Type.DISCONNECT -> {
                processDisconnect()
            }
            KotlinChatMessage.Type.ERROR -> {
                processError(message)
            }
            else -> TODO()
        }


    }

    private fun processConnect(message: KotlinChatMessage) {
        try{
            val givenId = parseDataFromServerMessage(message)["id"]
            id = givenId
            if(id != null){
                isConnected = true
                println("Connected to chat with id $id")
            }
        }catch (e: Exception){
            println("Error parsing CONNECT message: ${e.message}")
        }
    }

    private fun processDisconnect() {
        tcpConnectionScope.cancel()
        readScope.cancel()
        isConnected = false
        isTCPConnected = false
        if(!isTCPConnected) {
            println("Disconnected from server.")
        }
    }

    private fun processError(message: KotlinChatMessage) {
        println("Server returned error: ${parseDataFromServerMessage(message)["error"]}")
        lastError = message
    }

}
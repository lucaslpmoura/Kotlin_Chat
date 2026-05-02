package com.lucaslpmoura.kotlin_chat.client

import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage
import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage.Type
import com.lucaslpmoura.kotlin_chat.common.MAX_MESSAGE_SIZE
import com.lucaslpmoura.kotlin_chat.common.getMessageFromBytes
import com.lucaslpmoura.kotlin_chat.common.toByteArray
import com.lucaslpmoura.kotlin_chat.server.KotlinChatRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.Socket
import kotlin.time.Duration.Companion.milliseconds

class KotlinChatClient {

    val serverAddress = "localhost"
    val serverPort = 7960

    private lateinit var socket: Socket

    @Volatile
    var isTCPConnected: Boolean = false

    @Volatile
    var isConnected: Boolean = false

    var name: String? = null
        private set
    var id: String? = null
        private set

    var serverRooms: Map<String, String> = mapOf<String, String>()

    var lastError: KotlinChatMessage? = null

    private val tcpConnectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val readScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    public fun run() {

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
                if (isTCPConnected) {
                    val message = readMessage()
                    processMessage(message)
                } else {
                    delay(100.milliseconds)
                }
            }
        }
    }

    public fun connect(name: String) {
        if (!isTCPConnected) throw IOException("Socket is not connected.")
        val message = KotlinChatMessage("SELF", KotlinChatMessage.Type.CONNECT, name)
        socket.outputStream.write(message.toByteArray())
    }

    public suspend fun disconnect(disconnectId: String = id!!) {
        if (!isTCPConnected) throw IOException("Socket is not connected.")
        val message = KotlinChatMessage(
            "SELF",
            KotlinChatMessage.Type.DISCONNECT,
            disconnectId ?: throw IOException("Id is not set.")
        )
        socket.outputStream.write(message.toByteArray())
        delay(100.milliseconds)
        processDisconnect()
    }

    public fun listRooms() {
        if (!isTCPConnected) throw IOException("Socket is not connected.")
        if (!isConnected) throw IOException("You are not connected to the server.")
        val message = KotlinChatMessage(origin = "SELF", KotlinChatMessage.Type.LIST_ROOMS, id!!)
        sendMessage(message)

    }

    private fun sendMessage(message: KotlinChatMessage) {
        try {
            socket.outputStream.write(message.toByteArray())
        }catch (e: IOException) {
            throw Exception("Failed to write to server: ${e.message}")
        }
    }


    private fun readMessage() : KotlinChatMessage {
        val buffer = ByteArray(MAX_MESSAGE_SIZE)
        try{
            socket.inputStream.read(buffer)

        }catch (e: IOException){
            throw Exception("Failed to read from server: ${e.message}")
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
            KotlinChatMessage.Type.LIST_ROOMS -> {
                processListRoom(message)
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
            throw Exception("Error parsing CONNECT message: ${e.message}")
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

    private fun processListRoom(message: KotlinChatMessage) {
        try{
            serverRooms = parseDataFromServerMessage(message)
        }catch (e: Exception){
            throw Exception("Error parsing LIST_ROOM message: ${e.message}")
        }
    }

    private fun processError(message: KotlinChatMessage) {
        lastError = message
        throw Exception("Server returned error: ${parseDataFromServerMessage(message)["error"]}")
    }



    fun parseDataFromServerMessage(message : KotlinChatMessage) : Map<String, String>{
        return when(message.type) {
            Type.CONNECT -> {
                if(message.data.isEmpty()){
                    throw Exception("id not present.")
                }
                mapOf("id" to message.data)
            }
            Type.LIST_ROOMS -> {
                val serverRooms = mutableMapOf<String, String>()

                if(!message.data.isEmpty()){
                    val splitData = message.data.split('|')

                    /*
                    Creates a map such that:
                    map[roomId] == roomName
                     */
                    for(i in splitData.indices step 2){
                        try{
                            serverRooms[splitData[i]] = splitData[i+1]
                        }catch (e: IndexOutOfBoundsException){
                            break
                        }


                    }
                }
                serverRooms.toMap<String, String>()
            }
            Type.ERROR -> {
                mapOf("error" to message.data)
            }
            else -> {
                mapOf<String, String>("content" to "")
            }
        }
    }

}
package com.lucaslpmoura.kotlin_chat.client

import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage
import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage.Type
import com.lucaslpmoura.kotlin_chat.common.MAX_MESSAGE_SIZE
import com.lucaslpmoura.kotlin_chat.common.SERVER_PORT
import com.lucaslpmoura.kotlin_chat.common.getMessageFromBytes
import com.lucaslpmoura.kotlin_chat.common.toByteArray
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

    var serverAddress = "localhost"
    val serverPort = SERVER_PORT

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
    var currentRoomId: String? = null

    var lastError: KotlinChatMessage? = null
    var lastText: KotlinChatMessage? = null


    private lateinit var readScope : CoroutineScope

    public fun run() {
        println("Starting client...")
        socket = Socket(serverAddress, serverPort)
        isTCPConnected = true

        readScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        readScope.launch {
            println("Starting read scope...")
            while (true) {
                if (isTCPConnected) {
                    val message = readMessage()
                    processMessage(message)
                } else {
                    delay(100.milliseconds)
                }
            }
        }

        println("Client started.")
    }

    public fun connect(desiredName: String) {
        checkTCPConnection()
        val message = KotlinChatMessage("SELF", KotlinChatMessage.Type.CONNECT, desiredName)
        name = desiredName
        socket.outputStream.write(message.toByteArray())
    }

    public suspend fun disconnect(disconnectId: String = id!!) {
        checkTCPConnection()
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
        checkConnection()
        val message = KotlinChatMessage(origin = "SELF", KotlinChatMessage.Type.LIST_ROOMS, id!!)
        sendMessage(message)

    }

    public fun joinRoom(roomId: String) {
        checkConnection()
        val message = KotlinChatMessage("SELF", KotlinChatMessage.Type.JOIN_ROOM, "$id|$roomId")
        sendMessage(message)
    }

    public fun leaveRoom(roomId: String = currentRoomId!!) {
        checkConnection()
        val message = KotlinChatMessage("SELF", KotlinChatMessage.Type.LEAVE_ROOM, "$id|$roomId")
        sendMessage(message)
    }

    public fun text(text: String, roomId: String = currentRoomId!!) {
        checkConnection()
        if(currentRoomId == null) {
            throw Exception("You are not connected to any room.")
        }
        val message = KotlinChatMessage("SELF", KotlinChatMessage.Type.TEXT, "$id|$roomId|$text")
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
        println("Processing message of type ${message.type.name}")
        when(message.type) {
            Type.CONNECT -> processConnect(message)

            Type.DISCONNECT -> processDisconnect()

            Type.LIST_ROOMS -> processListRoom(message)

            Type.JOIN_ROOM -> processJoinRoom(message)

            Type.LEAVE_ROOM -> processLeaveRoom(message)

            Type.TEXT -> processText(message)

            Type.ERROR -> processError(message)


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
        readScope.cancel()
        isConnected = false
        isTCPConnected = false

        id = null
        name = null
        if(!isTCPConnected) {
            println("Disconnected from server.")
        }else{
            socket.close()
        }
    }

    private fun processListRoom(message: KotlinChatMessage) {
        try{
            serverRooms = parseDataFromServerMessage(message)
        }catch (e: Exception){
            throw Exception("Error parsing LIST_ROOM message: ${e.message}")
        }
    }

    private fun processJoinRoom(message: KotlinChatMessage) {
        try{
            currentRoomId = parseDataFromServerMessage(message)["id"]
        }catch (e: Exception){
            throw Exception("Error parsing JOIN_ROOM message: ${e.message}")
        }
    }

    private fun processLeaveRoom(message: KotlinChatMessage) {
        try {
            val serverRoomId = parseDataFromServerMessage(message)["id"]
            println("Server room id: $serverRoomId")
            println("Current room id: $currentRoomId")
            if (serverRoomId == currentRoomId) {
                currentRoomId = null
            }
        }catch (e: Exception){
            throw Exception("Error parsing LEAVE_ROOM message: ${e.message}")
        }
    }

    private fun processText(message: KotlinChatMessage) {
        try{
            val originUser = parseDataFromServerMessage(message)["originUser"] ?: throw Exception("Origin user not provided.")
            val roomId = parseDataFromServerMessage(message)["roomId"] ?: throw Exception("Room id not provided.")
            val text = parseDataFromServerMessage(message)["text"] ?: throw Exception("Text not provided.")
            if(roomId != currentRoomId){
                throw Exception("Client not connected to room $roomId")
            }


            lastText = KotlinChatMessage(originUser, Type.TEXT, text)
            println("LAST TEXT: ${lastText?.data}")
        }catch (e: Exception){
            throw Exception("Error parsing TEXT message: ${e.message}")
        }
    }

    private fun processError(message: KotlinChatMessage) {
        lastError = message
    }



    fun parseDataFromServerMessage(message : KotlinChatMessage) : Map<String, String>{
        return when(message.type) {
            Type.CONNECT, Type.JOIN_ROOM, Type.LEAVE_ROOM -> {
                if(message.data.isEmpty()){
                    throw Exception("id not present.")
                }
                mapOf("id" to message.data)
            }
            Type.LIST_ROOMS -> {
                val serverRooms = mutableMapOf<String, String>()

                if(!message.data.isEmpty()) {
                    val splitData = message.data.split('|')

                    /*
                    Creates a map such that:
                    map[roomId] == roomName
                     */
                    for (i in splitData.indices step 2) {
                        try {
                            serverRooms[splitData[i]] = splitData[i + 1]
                        } catch (e: IndexOutOfBoundsException) {
                            break
                        }


                    }
                }else{
                    throw Exception("message is empty.")
                }
                serverRooms.toMap<String, String>()
            }

            Type.TEXT -> {
                lateinit var originUser : String
                lateinit var roomId : String
                lateinit var text : String

                if(message.data.isEmpty()){
                    throw Exception("message is empty.")
                }

                val splitData = message.data.split('|')
                try{
                    originUser = splitData[0]
                    roomId = splitData[1]
                    text = splitData[2]
                }catch (e: IndexOutOfBoundsException){
                    throw Exception("originUser, roomId or text is null.")
                }


                if(originUser.isEmpty()){
                    throw Exception("invalid originUser")
                }
                if(roomId.isEmpty()){
                    throw Exception("invalid roomId")
                }
                if(text.isEmpty()){
                    throw Exception("text is empty.")
                }

                mapOf("originUser" to originUser, "roomId" to roomId, "text" to text)
            }
            Type.ERROR -> {
                mapOf("error" to message.data)
            }
            else -> {
                mapOf<String, String>("content" to "")
            }
        }
    }

    private fun checkTCPConnection() {
        if (!isTCPConnected) throw IOException("Socket is not connected.")
    }

    private fun checkConnection() {
        checkTCPConnection()
        if (!isConnected) throw IOException("You are not connected to the server.")
    }
}